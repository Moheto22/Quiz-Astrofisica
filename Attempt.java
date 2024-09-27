import java.util.ArrayList;

/**
 * @author Mohamed Boutanghach
 * La clase Attempt representa un intento de un usuario en el quiz.
 * Cada intento almacena la puntuación obtenida, el número de preguntas respondidas,
 * las dificultades seleccionadas y el usuario que realizó el intento.
 */
public class Attempt implements Comparable<Attempt> {
    // Puntuación obtenida en el intento.
    private final double points;

    // Número de preguntas respondidas en el intento.
    private final int numberQuestions;

    // Lista de dificultades asociadas a este intento.
    private final ArrayList<String> dificultys;

    // Usuario que realizó el intento.
    private final User user;

    /**
     * Constructor que inicializa un intento con los parámetros especificados.
     *
     * @param points Puntuación obtenida en el intento.
     * @param numberQuestions Número de preguntas respondidas en el intento.
     * @param dificultys Lista de dificultades seleccionadas en el intento.
     * @param user Usuario que realizó el intento.
     */
    public Attempt(double points, int numberQuestions, ArrayList<String> dificultys, User user) {
        this.points = points;
        this.numberQuestions = numberQuestions;
        this.dificultys = dificultys;
        this.user = user;
    }

    /**
     * Devuelve la puntuación obtenida en el intento.
     *
     * @return Puntuación obtenida.
     */
    public double getPoints() {
        return points;
    }

    /**
     * Devuelve el número de preguntas respondidas en el intento.
     *
     * @return Número de preguntas.
     */
    public int getNumberQuestions() {
        return numberQuestions;
    }

    /**
     * Devuelve la lista de dificultades asociadas a este intento.
     *
     * @return Lista de dificultades.
     */
    public ArrayList<String> getDificultys() {
        return dificultys;
    }

    /**
     * Devuelve el usuario que realizó el intento.
     *
     * @return Usuario que realizó el intento.
     */
    public User getUser() {
        return user;
    }

    /**
     * Convierte el intento a una cadena de texto que incluye la información
     * del usuario, la puntuación obtenida, el número de preguntas y las dificultades seleccionadas.
     *
     * @return Representación en forma de cadena del intento.
     */
    @Override
    public String toString() {
        return "Usuario: " + this.user.getName() +
                " Puntuación: " + this.points +
                " Numero de preguntas: " + this.numberQuestions +
                " Dificultades: " + generateStringDifi();
    }

    /**
     * Genera una cadena de texto con las dificultades seleccionadas, separadas por comas.
     *
     * @return Cadena con las dificultades seleccionadas.
     */
    private String generateStringDifi() {
        StringBuilder stb = new StringBuilder();
        for (int i = 0; i < this.dificultys.size(); i++) {
            stb.append(dificultys.get(i));
            if (i != this.dificultys.size() - 1) {
                stb.append(", ");
            }
        }
        return stb.toString();
    }

    /**
     * Compara dos intentos según la puntuación obtenida.
     * Sirve para ordenar intentos de mayor a menor puntuación.
     *
     * @param o El otro intento a comparar.
     * @return Un valor negativo, cero o positivo si este intento tiene menor,
     * igual o mayor puntuación que el otro intento.
     */
    @Override
    public int compareTo(Attempt o) {
        return Double.compare(o.getPoints(), this.points);
    }
}