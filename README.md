# Jeu du Moulin (Nine Men's Morris)

Bienvenue sur le projet **Jeu du Moulin** (ou Merelle), développé en **Java** en utilisant le framework **Boardifier** et **JavaFX**.

---

## Modes de Jeu

Au lancement, l'application propose trois modes de jeu :
* **Joueur vs Joueur** : Affrontez un ami sur la même machine.
* **Joueur vs Ordinateur** : Défiez notre IA (`StrategieMoulinIA`).
* **Ordinateur vs Ordinateur** : Regardez deux IA s'affronter.

---

## Fonctionnalités Principales

* **IA Intelligente (`StrategieMoulinIA`)** : Analyse le plateau pour former ses propres moulins et bloquer activement les vôtres.
* **Gestion Complète des Phases** : 
    1. *Phase de placement* : Pose des pions sur le plateau.
    2. *Phase de déplacement* : Glissement des pions vers les intersections voisines.
    3. *Phase de vol* : Quand un moulin est crée un pion adverse est enlevé
* **Interface Graphique** : Un rendu visuel propre et interactif fait avec JavaFX.

---

## Installation et Lancement

### Prérequis
* **Java JDK 17** (ou supérieur)
* **JavaFX correspondant au JdK choisi
* **IntelliJ IDEA** (recommandé)

### Étapes d'installation

1. **Cloner le projet** :
   ```bash
   git clone [https://github.com/votre-username/nom-du-repo.git](https://github.com/maxencegarcia/merelle.git)
2. **Ouvrir le projet avec intellij idea**
3. **Ajouter JavaFx dans les librairies du projet**
4. **Ajouter les options de JVM dans la configuration de lancement
   ```bash
   --module-path /home/..../JavaFX/lib/ --add-modules javafx.controls,javafx.fxml
5. **Lancer le jeu avec la flèche verte en haut de l'interface de idea**
