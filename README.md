# Jeu du Moulin (Nine Men's Morris)

Salut ! Bienvenue sur notre projet de **Jeu du Moulin** (ou Merelle pour les intimes). C'est un petit projet Java fait avec le framework **Boardifier**.

On a essayé de faire un truc propre où tu peux jouer contre tes potes ou contre une IA qui se débrouille pas trop mal.

## Comment lancer le bouzin ?

C'est super simple, il te faut juste Java installé sur ta machine.

1. Tu compiles tout :
   ```bash
    javac *.java
   ```
2. Tu lances le jeu :
   ```bash
   java Merelle
   ```

## C'est quoi les modes ?

Quand tu lances le jeu, tu as le choix entre :
- **Mode 1 : Joueur vs Joueur** - Le grand classique pour régler ses comptes.
- **Mode 2 : Joueur vs Ordinateur** - Si tu n'as pas d'amis sous la main (ou si tu veux tester l'IA).
- **Mode 3 : Ordinateur vs Ordinateur** - Juste pour regarder deux bots se battre. C'est assez relax. Ou pour écrire des orders c'est assez pratique

## Ce qu'on a mis dedans

- Une **IA (StrategieMoulinIA)** qui essaie de faire des moulins et de te bloquer quand tu es sur le point d'en faire un.
- Une gestion des phases (Placement, Déplacement, Vol de pion).
- Un affichage console pas parfait mais qui fait le taff.

## Tech
- **Java** (évidemment)
- **Boardifier** (le framework de base pour le plateau)

