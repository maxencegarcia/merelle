class Jeu {
    private Plateau plateau;
    private Joueur joueur1;
    private Joueur joueur2;
    private Joueur joueurActuel;
    private Phase phaseActuelle;

    public Jeu(String j1, String j2) {
        plateau = new Plateau();
        joueur1 = new Joueur(j1, Couleur.BLANC, 9);
        joueur2 = new Joueur(j2, Couleur.NOIR, 9);
        joueurActuel = joueur1;
        phaseActuelle = Phase.PLACE;
    }

    public void jouer() {
        while (!estTermine()) {

            if (phaseActuelle == Phase.PLACE) {

            } else {

            }
        }
        afficherGagnant();
    }

    public boolean estTermine() {
        return phaseActuelle != Phase.PLACE && (joueur1.compterPions() < 3 || joueur2.compterPions() < 3);
    }

    public void afficherGagnant() {
        if (joueur1.compterPions() < 3) {
            System.out.println("Joueur 2 gagnant");
        } else if (joueur2.compterPions() < 3) {
            System.out.println("Joueur 1 gagnant");
        }
    }
}
