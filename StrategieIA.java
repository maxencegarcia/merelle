public interface StrategieIA {
    Position choisirPlacement(Couleur maCouleur, Couleur couleurAdverse);
    Position[] choisirDeplacement(Joueur joueur);
    Position choisirVol(Couleur couleurAdverse);
}
