
public interface    AIStrategy {
    Position choosePlacement(Color myColor, Color enemyColor);
    Position[] chooseMove(PlayerC player);
    Position chooseSteal(Color enemyColor);
}