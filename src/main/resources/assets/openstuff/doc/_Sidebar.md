# OpenStuff

OpenStuff est un mod qui ajoute à OpenComputer une armure.

### API:
```lua
setColor(number:color) --met une couleur a l'armure , color est un nombre entre 0 et 15.

getColor() --renvois la couleur actuel.

displayText(string:text,number:x,number:y,number:color[0x000000-0xFFFFFF]) --affiche du texte dans l'HUD du joueur.

notDisplayText(string:text,number:x,number:y) --supprime le texte spécifié au coordonné spécifié.

getDimensionIndex() --renvois l'ID de la dimension dans laquelle ce trouve le joueur.

getPosition() --renvois les coordonne x y z du joueur.

getBedLocation() --renvois les coordonne x y z du lit du joueur.

hasBedLocation() --renvois si le joueur a un lit.

getDisplayName() --renvois le nom du joueur.

getExperienceLevel() --renvois le niveau d'XP du joueur.

getFoodLevel() --renvois le niveau de nourriture du joueur.

getSaturationLevel() --renvois le niveau de saturation du joueur.

getHealth() --renvois le niveau de Santer du joueur. 
```

