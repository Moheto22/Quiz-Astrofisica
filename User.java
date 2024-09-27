import java.util.ArrayList;

/**
 * @author Mohamed Boutanghach
 * La clase User representa a un usuario que participa en el quiz.
 * Cada usuario tiene un nombre y una lista de intentos (attempts) asociados.
 */
public class User {
    // El nombre del usuario.
    private final String name;

    // Lista que almacena los intentos (Attempt) realizados por el usuario.
    private ArrayList<Attempt> attempts;

    /**
     * Constructor que inicializa un usuario con un nombre específico.
     *
     * @param name El nombre del usuario.
     */
    public User(String name) {
        this.name = name;
        this.attempts = new ArrayList<>(); // Inicializa la lista de intentos como una nueva lista vacía
    }

    /**
     * Obtiene el nombre del usuario.
     *
     * @return El nombre del usuario.
     */
    public String getName() {
        return name;
    }

    /**
     * Añade un intento (Attempt) a la lista de intentos del usuario.
     *
     * @param attempt El intento que se añade al historial de intentos del usuario.
     */
    public void addAttempt(Attempt attempt) {
        this.attempts.add(attempt); // Añade un nuevo intento a la lista de intentos del usuario
    }
}
