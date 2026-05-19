public interface StrategieIA {
    Position choisirPlacement(Couleur maCouleur, Couleur couleurAdverse);
    Position[] choisirDeplacement(PlayerC playerC);
    Position choisirVol(Couleur couleurAdverse);
}
