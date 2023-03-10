--[[
===========
 Liskel OS
===========

Description:
A barebones operating system that is
designed to give the programs full
control over the system. Boots up to
a lua interpreter if autorun.lua
does not exist.
]]
_osname = 'Armor Shell based to Liskel OS'
_osversion = '0.1 - 2.2.1'
-- ========== INIT START ========== --
--+-+-+-+-+ Require +-+-+-+-+--
function require(pkg)
    if type(pkg) ~= 'string' then return nil end
    return _ENV[pkg] or _G[pkg]
end
--+-+-+-+-+ Component +-+-+-+-+--
setmetatable(component, { __index = function(_, k) return component.getPrimary(k) end })
fsaddr = component.invoke(component.list("eeprom")(), "getData")
local _component_primaries = {}
_component_primaries['filesystem'] = component.proxy(fsaddr)
function component.setPrimary(dev, addr)
    for k,v in component.list() do
        if k == addr and v == dev then
            _component_primaries[dev] = component.proxy(addr)
        end
    end
end
function component.getPrimary(dev)
    if _component_primaries[dev] == null then
        for k, v in component.list() do
            if v == dev then component.setPrimary(v,k) break end
        end
    end
    return _component_primaries[dev]
end
--+-+-+-+-+ Graphics +-+-+-+-+--
g = {
    bind = function()
        local screen = component.list('screen')()
        return component.gpu.bind(screen)
    end,
    available = function()
        local gpu, screen = component.list('gpu')(), component.list('screen')()
        return (gpu and screen)
    end,
    copy = component.gpu.copy,
    getResolution = component.gpu.getResolution,
    setResolution = component.gpu.setResolution,
    getDepth = component.gpu.maxDepth,
    setBG = component.gpu.setBackground,
    setFG = component.gpu.setForeground,
    fill = function(x, y, w, h)
        return component.gpu.fill(x, y, w, h, ' ')
    end,
    fillc = component.gpu.fill,
    drawText = component.gpu.set,
}
local w, h = g.getResolution()

--+-+-+-+-+ Error +-+-+-+-+--
std_error = error
function error(msg)
    g.setFG(0xFFFFFF)
    g.setBG(0x000000)
    g.fill(1, 1, w, h)
    -- write to screen
    local line = 1
    local prev = 1
    for wo in string.gmatch(msg, '()\n') do
        g.drawText(1, line, msg:sub(prev, wo - 1))
        prev = wo + 1
        line = line + 1
    end
    g.drawText(1, line, msg:sub(prev))
    repeat
        local e, _, _, code = computer.pullSignal()
    until e == 'key_down' and code == 28
    computer.shutdown(true)
end
-- ========== INIT END ========== --
console = {}
-- table serialize function
function console.Serialize(o, depth)
    local function ser(ob, d, ind)
        ind = ind or 1
        d = d or 3
        d = d - 1
        local indn = 0
        local indent = ''
        while indn < ind do
            indent = indent .. '  '
            indn = indn + 1
        end
        local retstr = ''
        if type(ob) == "number" then
            retstr = retstr .. ob .. ''
        elseif type(ob) == "boolean" then
            retstr = retstr .. tostring(ob)
        elseif type(ob) == "string" then
            retstr = retstr .. string.format("%q", ob) .. ''
        elseif type(ob) == "table" then
            if d <= 0 then return '<' .. type(ob) .. '>' end
            retstr = retstr .. '{\n'
            for k, v in pairs(ob) do
                retstr = retstr .. indent .. '' .. k .. '='
                retstr = retstr .. ser(v, d, ind + 1)
                retstr = retstr .. ',\n'
            end
            retstr = retstr .. indent:sub(1, indent:len() - 2) .. '}'
        else
            retstr = retstr .. '<' .. type(ob) .. '>'
        end
        return retstr
    end
    return ser(o, depth)
end
-- array to string / string to array
function console.ArrayToStr(tabl)
    local retval = ''
    for i = 1, #tabl do
        retval = retval .. tabl[i]
    end
    return retval
end
function console.StrToArray(str)
    local retval = {}
    for i = 1, #str do
        table.insert(retval, str:sub(i, i))
    end
    return retval
end
-- line output
function console.lineout(str, line)
    g.fill(1, line, w, 1)
    g.drawText(1, line, str)
end
function console.lineoutoff(str, line, offs)
    offs = offs or 1
    g.fill(offs, line, w, 1)
    g.drawText(offs, line, str)
end
-- history buffer
console.history = { --  history system
    mem = {}, -- output lines history
    cmdmem = {}, -- command history
    size = h * 10, -- history size (10 screens)
    viewheight = h - 1, -- height of viewport
    viewbottom = 1, -- current viewport bottom line
    prevbottom = 1, -- previous view bottom
    recallptr = 1, -- pointer for line recall fuction
    printoffset = 1, -- print offset x-axis
    lnum = 1, -- line number accumulator
    scrspeed = 5, -- scroll speed
    scrdir = 0, -- scroll direction
}
function console.history.PrintAll()
    -- reprint viewport
    if next(console.history.mem) == nil then return end
    for i = 1, console.history.viewheight - 1 do
        local bot = console.history.viewbottom - (i - 1)
        if bot <= 0 then return end
        local toprint = console.history.mem[bot]
        local cpos = console.history.viewheight - (i - 1)
        console.lineoutoff(toprint, cpos, console.history.printoffset)
    end
end
function console.history.Update()
    -- update viewport
    if next(console.history.mem) == nil then return end
    if console.history.scrdir == 0 then
        console.history.PrintAll()
    elseif console.history.scrdir > 0 then
        -- user scrolled up
        g.copy(1,2, w, h-console.history.scrspeed-2, 0, console.history.scrspeed)
        for i = 1, console.history.scrspeed do
            local bot = (console.history.viewbottom - console.history.viewheight + 1 + console.history.scrspeed) - (i - 1)
            if bot <= 0 then return end
            local toprint = console.history.mem[bot]
            local cpos = 1 + console.history.scrspeed - (i - 1)
            console.lineoutoff(toprint, cpos, console.history.printoffset)
        end
    elseif console.history.scrdir < 0 then
        -- user scrolled down
        g.copy(1,2 + console.history.scrspeed, w, h-console.history.scrspeed-2, 0, -console.history.scrspeed)
        for i = 1, console.history.scrspeed do
            local bot = console.history.viewbottom - (i - 1)
            if bot <= 0 then return end
            local toprint = console.history.mem[bot]
            local cpos = console.history.viewheight - (i - 1)
            console.lineoutoff(toprint, cpos, console.history.printoffset)
        end
    end
    console.history.scrdir = 0 -- reset
end
function console.history.ScrollEnd()
    console.history.prevbottom = console.history.viewbottom
    console.history.viewbottom = #console.history.mem
    console.history.PrintAll() -- reprint history
end
function console.history.ScrollTop()
    console.history.prevbottom = console.history.viewbottom
    console.history.viewbottom = h - 2
    console.history.PrintAll() -- reprint history
end
function console.history.ScrollUp(scr)
    scr = scr or 1
    if #console.history.mem < console.history.viewheight then return end
    console.history.prevbottom = console.history.viewbottom
    console.history.viewbottom = console.history.viewbottom - scr
    console.history.scrdir = 1
    if console.history.viewbottom <= console.history.viewheight - 1 then
        console.history.scrdir = 0
        console.history.viewbottom = console.history.viewheight - 1
    end
    console.history.Update() -- print out history
end
function console.history.ScrollDown(scr)
    scr = scr or 1
    console.history.prevbottom = console.history.viewbottom
    console.history.viewbottom = console.history.viewbottom + scr
    console.history.scrdir = -1
    if console.history.viewbottom >= #console.history.mem then
        console.history.scrdir = 0
        console.history.viewbottom = #console.history.mem
    end
    console.history.Update() -- print out history
end
function console.history.MoveRecall(pos)
    if next(console.history.cmdmem) == nil then return end
    pos = pos or 0
    console.history.recallptr = console.history.recallptr + pos
    if console.history.recallptr >= #console.history.cmdmem then
        console.history.recallptr = #console.history.cmdmem
    elseif console.history.recallptr <= 1 then
        console.history.recallptr = 1
    end
end
function console.history.ResetRecall()
    console.history.recallptr = #console.history.cmdmem
end
function console.history.Recall()
    if next(console.history.cmdmem) == nil then return "" end
    return console.history.cmdmem[console.history.recallptr]
end
function console.history.Add(str)
    str = " " .. tostring(console.history.lnum) .. "  | " .. str
    table.insert(console.history.mem, str)
    console.history.viewbottom = #console.history.mem
    console.history.recallptr = #console.history.cmdmem + 1
    console.history.Update()
    console.history.lnum = console.history.lnum + 1
end
function console.history.AddInp(str)
    table.insert(console.history.cmdmem, str)
    console.history.Add(str)
end
-- input buffer
console.input = {
    buffer = {}, -- input buffer
    col = 1, -- current input column
    printoffset = 1, -- print offset
}
function console.input.Print()
    -- print input
    local out = console.ArrayToStr(console.input.buffer)
    console.lineoutoff(out, h, console.input.printoffset)
end
function console.input.SetPrintOffset(offs)
    console.input.printoffset = offs
end
function console.input.Append(str)
    -- append to input buffer
    table.insert(console.input.buffer, str)
    console.input.Print()
end
function console.input.Insert(str, pos)
    -- insert in input buffer
    pos = pos or console.input.col
    table.insert(console.input.buffer, pos, str)
    console.input.Print()
end
function console.input.SetPos(pos)
    -- set cursor position
    pos = pos or #console.input.buffer
    if pos < 1 then pos = 1 end
    if pos > #console.input.buffer then pos = #console.input.buffer + 1 end
    console.input.col = pos
end
function console.input.MovePos(mov)
    -- move cursor position
    local pos = console.input.col + mov
    console.input.SetPos(pos)
end
function console.input.GetCharAtPos()
    return console.input.buffer[console.input.col]
end
function console.input.GetString()
    return console.ArrayToStr(console.input.buffer)
end
function console.input.DelChar()
    table.remove(console.input.buffer, console.input.col)
    console.input.Print()
end
function console.input.Clear()
    console.input.buffer = {}
    console.input.col = 1
    console.input.Print()
end
function console.input.SetBuffer(str)
    console.input.buffer = console.StrToArray(str)
    console.input.col = #console.input.buffer + 1
    console.input.Print()
end
-- print function
function console.print(str)
    if type(str) == 'table' then
        str = console.Serialize(str)
    else
        str = tostring(str)
    end
    local prev = 1
    for wo in string.gmatch(str, '()\n') do
        console.history.Add(str:sub(prev, wo - 1))
        prev = wo + 1
    end
    console.history.Add(str:sub(prev))
end

function toList(str)
    local t = {}
    for token in string.gmatch(str, "[^%s]+") do
        if token == "true" then
            table.insert(t,true)
        elseif token == "false" then
            table.insert(t,false)
        elseif tonumber(token) ~= nil then
            table.insert(t,tonumber(token))
        else
            table.insert(t,token)
        end
    end
    return t
end

-- console function
function console.Run()
    component.modem.open(1)
    -- screen clear
    g.fill(1, 2, w, h)
    -- print os header
    g.setFG(0x000000)
    g.setBG(0xFFFFFF)
    g.fill(1, 1, w, 1)
    g.drawText(1, 1, "  " .. _osname .. " " .. _osversion)
    g.setFG(0xFFFFFF)
    g.setBG(0x000000)
    -- console init
    local console_header = ">> "
    local blinkon = true
    local hist = console.history
    local inp = console.input
    print = console.print
    console.lineout(console_header, h)
    inp.SetPrintOffset(#console_header + 1)
    -- intro text
    print("* F12 to restart  *")
    print("* F10 to shutdown *")
    -- console loop start
    while true do
        g.setFG(0x000000)
        g.setBG(0xFFFFFF)
        local energyPc = string.format("%.0f",(100*computer.energy()/computer.maxEnergy()))
        if tonumber(energyPc) < 30 then
            g.setFG(0xFF0000)
        end
        g.drawText(w - (string.len(energyPc)+2), 1, energyPc .."%")
        g.setFG(0xFFFFFF)
        g.setBG(0x000000)
        local evt = table.pack(computer.pullSignal(0.4))
        if evt[1] == 'key_down' then
            if evt[4] == 88 then computer.shutdown(true) end -- F12 to restart
            if evt[4] == 68 then computer.shutdown() end -- F10 to shutdown
            -- command
            if evt[4] == 28 then -- enter key
                hist.AddInp(inp.GetString()) -- add input to history
                console.lineout(console_header, h)
                -- parse command --
                local cmd = inp.GetString() -- get string
                -- execute --
                local sended , reson = component.modem.broadcast(1,"openarmor",table.unpack(toList(cmd)))
                if sended ~= true then
                    print(reson)
                end
                -- done --
                inp.Clear() -- clear input buffer
            elseif evt[4] == 14 then -- backspace
                if inp.col > 1 then
                    inp.MovePos(-1)
                    inp.DelChar()
                    hist.ResetRecall()
                end
            elseif evt[4] == 200 then -- up key
                hist.MoveRecall(-1)
                inp.SetBuffer(hist.Recall())
            elseif evt[4] == 208 then -- down key
                hist.MoveRecall(1)
                inp.SetBuffer(hist.Recall())
            elseif evt[4] == 203 then -- left key
                inp.MovePos(-1)
            elseif evt[4] == 205 then --  right key
                inp.MovePos(1)
            elseif evt[4] == 199 then -- home
                inp.MovePos(-99999)
            elseif evt[4] == 207 then -- end
                inp.MovePos(99999)
            elseif evt[3] ~= 0 then -- printable keys
                local char = string.char(evt[3])
                inp.Insert(char)
                inp.MovePos(1)
            end
        elseif evt[1] == 'scroll' then
            if evt[5] > 0 then -- scroll up
                hist.ScrollUp(hist.scrspeed)
            elseif evt[5] < 0 then -- scroll down
                hist.ScrollDown(hist.scrspeed)
            end
        elseif evt[1] == 'modem_message' then
            for j = 6, #evt do
                if type(evt[j]) == "string" then
                    print(evt[j])
                elseif type(evt[j]) == "number" then
                    print(tostring(evt[j]))
                elseif type(evt[j]) == "boolean" then
                    if evt[j] then
                        print("true")
                    else
                        print("false")
                    end
                else
                    print("unknow type returned")
                end
            end
        end
        if blinkon then -- cursor blink
            local posx = console.input.col + console.input.printoffset - 1
            g.setBG(0xFFFFFF)
            g.fill(posx, h, 1, 1)
            g.setBG(0x000000)
            blinkon = false
        else
            inp.Print()
            blinkon = true
        end
    end
end
-- console main
local function main()
    console.Run()
end

-- run shell
local res, err = xpcall(main, function(msg) return msg .. '\n' .. debug.traceback() end)
if not res and err then
    error(err)
end
computer.shutdown()