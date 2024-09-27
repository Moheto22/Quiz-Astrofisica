import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;

/**
 * @author Mohamed Boutanghach
 * Clase principal que contiene el flujo de ejecución del juego de preguntas y respuestas.
 */
public class Main {
    public static void main(String[] args) {
        // Variables iniciales
        int numberQuestions, numberCategorys; // Número de preguntas y categorías seleccionadas por el usuario
        double points = 0; // Puntos acumulados por el jugador
        boolean finish = false; // Bandera para indicar si el juego ha terminado
        int respons; // Respuesta del jugador
        int option; // Opción del menú seleccionada por el usuario
        int[] numberOfQuestionGame = {1}; // Número de preguntas en el juego actual
        int[] category = {0}; // Categoría seleccionada para las preguntas
        User userPlay; // Usuario que está jugando actualmente
        ArrayList<User> users = readUsers(); // Lista de usuarios leída de archivo
        ArrayList<Attempt> attempts = readAttempts(users); // Lista de intentos leída de archivo
        addAttemptsToUsers(users, attempts); // Agrega los intentos a los usuarios
        Collections.sort(attempts); // Ordena los intentos de mayor a menor puntaje
        int[] questionsPrinted = new int[20]; // Preguntas que ya se han mostrado
        double[] streak = {1}; // Racha de respuestas correctas
        ArrayList<ArrayList<String>> questions = fillOutQuestions(); // Lista de preguntas
        ArrayList<ArrayList<ArrayList<String>>> answeres = fillOutAnsweres(); // Lista de respuestas

        // Bucle principal del juego
        do {
            option = getOption(); // Obtiene la opción seleccionada por el usuario
            switch (option) {
                case 1:
                    // Caso en que se crea un nuevo usuario
                    generateUser(users);
                    break;

                case 2:
                    // Caso en que el usuario quiere jugar
                    if (users.isEmpty()) {
                        cantPlay(); // Si no hay usuarios, se muestra un mensaje
                    } else {
                        userPlay = choseUser(users); // Selección del usuario que jugará
                        numberQuestions = askNumberQuestion(); // Número de preguntas a responder
                        numberCategorys = askNumberCategorys(); // Número de categorías a elegir
                        int[] catagorysElection = selectionCategorys(numberCategorys); // Selección de categorías
                        int[] numberOfQuestions = selectionNumberQuestionsCategory(numberQuestions, numberCategorys); // Selección de preguntas por categoría
                        Arrays.fill(questionsPrinted, 0, 20, -1); // Rellena el array con valores -1

                        // Bucle para mostrar preguntas y recibir respuestas
                        do {
                            showPoints(points); // Muestra los puntos actuales
                            respons = printQuestionAndTakeAnswere(questions, answeres, numberOfQuestions, category, questionsPrinted, catagorysElection, numberOfQuestionGame);
                            if (respons == -1) {
                                finish = true; // Termina el juego si la respuesta es -1
                            } else {
                                points = checkAnswere(respons, points, streak, catagorysElection, category); // Verifica la respuesta y actualiza los puntos
                            }
                        } while (!finish);

                        result(points); // Muestra el resultado final del juego
                        attempts.add(new Attempt(points, numberQuestions, generateStringDif(catagorysElection), userPlay)); // Añade un nuevo intento
                        addAttemptInFile(attempts.get(attempts.size()-1)); // Guarda el intento en archivo
                        userPlay.addAttempt(attempts.get(attempts.size() - 1)); // Asocia el intento al usuario

                        // Ordena la lista de intentos de mayor a menor puntaje
                        Collections.sort(attempts);

                        // Resetea los valores para la siguiente partida
                        points = 0;
                        category[0] = 0;
                        numberOfQuestionGame[0] = 1;
                        finish = false;
                    }
                    break;

                case 3:
                    // Caso en que se muestran los mejores intentos
                    showBestAttempts(attempts);
                    break;
            }
        } while (option != 0); // El bucle se repite hasta que el usuario seleccione la opción 0

        goodBye(); // Muestra el mensaje de despedida
    }
    /**
     * Método que añade un intento (Attempt) a un archivo en disco.
     *
     * @param attempt El intento que se va a añadir al archivo.
     */
    private static void addAttemptInFile(Attempt attempt) {
        Path path = Paths.get("src/data/Attempts"); // Ruta del archivo donde se almacenan los intentos
        try {
            // Escribe una nueva línea en el archivo
            Files.writeString(path, System.lineSeparator(), StandardOpenOption.APPEND);
            // Escribe los detalles del intento: puntos, número de preguntas, dificultades concatenadas y el nombre del usuario
            Files.writeString(path, attempt.getPoints() + "," + attempt.getNumberQuestions() + "," + generateStringConcatDif(attempt.getDificultys()) + "," + attempt.getUser().getName(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            // En caso de error, muestra un mensaje de que la escritura no se pudo realizar
            System.out.println("La escritura en el fichero de intentos no se ha hecho bien");
        }
    }

    /**
     * Método que genera una cadena de texto con las dificultades concatenadas de una lista.
     *
     * @param dificultys Lista de dificultades (String) asociadas a un intento.
     * @return Cadena de texto con las dificultades concatenadas, separadas por ';'.
     */
    private static String generateStringConcatDif(ArrayList<String> dificultys) {
        StringBuilder strB = new StringBuilder(); // StringBuilder para la concatenación de las dificultades
        for (int i = 0; i < dificultys.size(); i++) {
            strB.append(dificultys.get(i)); // Añade cada dificultad al StringBuilder
            if (i != dificultys.size() - 1) {
                strB.append(";"); // Añade un separador ';' entre las dificultades, excepto después de la última
            }
        }
        return strB.toString(); // Devuelve la cadena concatenada
    }

    /**
     * Método que asocia los intentos a los usuarios correspondientes.
     *
     * @param users    Lista de usuarios.
     * @param attempts Lista de intentos.
     */
    private static void addAttemptsToUsers(ArrayList<User> users, ArrayList<Attempt> attempts) {
        User user;
        for (int i = 0; i < attempts.size(); i++) {
            // Busca el usuario correspondiente al nombre registrado en el intento
            user = getUser(attempts.get(i).getUser().getName(), users);
            // Añade el intento al usuario correspondiente
            user.addAttempt(attempts.get(i));
        }
    }

    /**
     * Método que lee los intentos almacenados en un archivo y los convierte en una lista de intentos.
     *
     * @param users Lista de usuarios a los que se asociarán los intentos.
     * @return Lista de intentos leídos del archivo.
     */
    private static ArrayList<Attempt> readAttempts(ArrayList<User> users) {
        Path path = Paths.get("src/data/Attempts"); // Ruta del archivo de intentos
        List<String> listAttempts = null; // Lista que almacenará las líneas del archivo
        String[] dataAttempts; // Array para almacenar los datos de cada línea
        String[] difArray; // Array que almacenará las dificultades
        User user;
        ArrayList<String> difArrayFinal; // Lista que almacenará las dificultades del intento
        ArrayList<Attempt> array = new ArrayList<>(); // Lista de intentos que se va a devolver

        try {
            // Lee todas las líneas del archivo de intentos
            listAttempts = Files.readAllLines(path);
        } catch (IOException e) {
            // Si ocurre un error al leer el archivo, muestra un mensaje
            System.out.println("El fichero de los intentos no se ha podido leer correctamente");
        }

        // Procesa cada línea del archivo
        for (int i = 0; i < listAttempts.size(); i++) {
            difArrayFinal = new ArrayList<>();
            // Divide cada línea por comas para obtener los datos del intento
            dataAttempts = listAttempts.get(i).split(",");
            // Divide las dificultades por el separador ';'
            difArray = dataAttempts[2].split(";");
            // Añade todas las dificultades a la lista final
            difArrayFinal.addAll(Arrays.asList(difArray));
            // Busca el usuario correspondiente al intento
            user = getUser(dataAttempts[3], users);
            // Crea un nuevo intento y lo añade a la lista
            array.add(new Attempt(Double.valueOf(dataAttempts[0]), Integer.valueOf(dataAttempts[1]), difArrayFinal, user));
        }

        // Devuelve la lista de intentos
        return array;
    }

    /**
     * Método que busca un usuario por su nombre en una lista de usuarios.
     *
     * @param name  Nombre del usuario a buscar.
     * @param users Lista de usuarios.
     * @return El usuario encontrado, o null si no se encuentra.
     */
    private static User getUser(String name, ArrayList<User> users) {
        int i = 0;
        boolean found = false;
        // Busca en la lista de usuarios hasta que encuentra una coincidencia o llega al final
        while (i < users.size() && !found) {
            if (name.equals(users.get(i).getName())) {
                found = true; // Si encuentra el usuario, marca found como true
            } else {
                i++; // Continúa buscando en la lista
            }
        }
        return users.get(i); // Devuelve el usuario encontrado
    }

    /**
     * Lee los usuarios almacenados en un archivo de texto y los carga en una lista.
     *
     * @return Una lista de objetos User que representa a los usuarios leídos del archivo.
     */
    private static ArrayList<User> readUsers() {
        ArrayList<User> users = new ArrayList<>(); // Lista que contendrá los usuarios
        Path path = Paths.get("src/data/Users"); // Ruta del archivo de usuarios
        List<String> listUsers = null; // Lista temporal que contendrá las líneas leídas del archivo

        try {
            listUsers = Files.readAllLines(path); // Lee todas las líneas del archivo de usuarios
        } catch (IOException e) {
            System.out.println("El fichero de los usuarios no se ha podido leer");
        }

        // Añade cada línea del archivo como un nuevo usuario, si la línea no está vacía
        for (int i = 0; i < listUsers.size(); i++) {
            if (!listUsers.get(i).equals("")) {
                users.add(new User(listUsers.get(i)));
            }
        }
        return users; // Devuelve la lista de usuarios
    }

    /**
     * Muestra los puntos acumulados del jugador.
     *
     * @param points Puntos actuales del jugador.
     */
    private static void showPoints(double points) {
        System.out.println("Puntos: " + points); // Muestra los puntos por consola
    }

    /**
     * Muestra un mensaje de despedida al usuario.
     */
    private static void goodBye() {
        System.out.println("Gracias por jugar"); // Mensaje de despedida
    }

    /**
     * Muestra las 5 mejores puntuaciones registradas en el sistema.
     *
     * @param attempts Lista de intentos (attempts) que contienen las puntuaciones de los usuarios.
     */
    private static void showBestAttempts(ArrayList<Attempt> attempts) {
        int i = 0;

        if (attempts.isEmpty()) {
            System.out.println("No se puede mostrar el ranking porque no hay intentos registrados");
        } else {
            // Muestra las mejores 5 puntuaciones (o menos si no hay suficientes intentos)
            while (i < attempts.size() && i < 5) {
                System.out.println((i + 1) + ")- " + attempts.get(i).toString());
                i++;
            }
        }
    }

    /**
     * Crea un nuevo usuario y lo añade a la lista y al archivo de usuarios.
     *
     * @param users Lista de usuarios del sistema.
     */
    private static void generateUser(ArrayList<User> users) {
        String name;
        System.out.println("Introduce el nombre del usuario");
        name = Teclat.llegirString(); // Lee el nombre del usuario

        // Mientras el nombre ya exista, pide otro
        while (searchUser(name, users)) {
            System.out.println("Este nombre ya existe, introduce otro");
            name = Teclat.llegirString();
        }

        // Escribe el nuevo usuario en el archivo de usuarios
        Path path = Paths.get("src/data/Users");
        try {
            Files.writeString(path, System.lineSeparator(), StandardOpenOption.APPEND);
            Files.writeString(path, name, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.out.println("Error en la escritura del fichero de usuarios");
        }

        // Añade el nuevo usuario a la lista
        users.add(new User(name));
    }

    /**
     * Comprueba si un usuario con el nombre dado ya existe en el sistema.
     *
     * @param name  Nombre del usuario a buscar.
     * @param users Lista de usuarios del sistema.
     * @return true si el usuario existe, false si no.
     */
    private static boolean searchUser(String name, ArrayList<User> users) {
        boolean found = false;
        int i = 0;

        // Busca el usuario en la lista ignorando mayúsculas y minúsculas
        while (i < users.size() && !found) {
            if (users.get(i).getName().equalsIgnoreCase(name)) {
                found = true; // Marca como encontrado si hay coincidencia
            } else {
                i++;
            }
        }

        return found; // Devuelve si el usuario fue encontrado o no
    }

    /**
     * Genera una lista de cadenas que representan las dificultades seleccionadas por el usuario.
     *
     * @param catagorysElection Array de enteros que indica las categorías seleccionadas.
     * @return Lista de cadenas que representan las dificultades.
     */
    private static ArrayList<String> generateStringDif(int[] catagorysElection) {
        ArrayList<String> dificultys = new ArrayList<>(); // Lista de dificultades

        // Asocia cada valor entero a una dificultad
        for (int i = 0; i < catagorysElection.length; i++) {
            if (catagorysElection[i] == 0) {
                dificultys.add("Facil");
            } else if (catagorysElection[i] == 1) {
                dificultys.add("Medio");
            } else if (catagorysElection[i] == 2) {
                dificultys.add("Dificil");
            } else if (catagorysElection[i] == 3) {
                dificultys.add("Extremo");
            }
        }

        return dificultys; // Devuelve la lista de dificultades
    }

    /**
     * Permite al usuario seleccionar con cuál usuario existente en el sistema desea jugar.
     *
     * @param users Lista de usuarios del sistema.
     * @return El usuario seleccionado para jugar.
     */
    private static User choseUser(ArrayList<User> users) {
        User user;
        int index;

        // Si hay más de un usuario, permite elegir uno
        if (users.size() > 1) {
            showUsers(users); // Muestra todos los usuarios disponibles
            System.out.println("Escoje un usuario");
            index = getResponsInt(1, users.size()); // Lee la selección del usuario
            user = users.get(index - 1); // Selecciona el usuario en base al índice
        } else {
            user = users.get(0); // Si solo hay un usuario, lo selecciona automáticamente
        }

        return user;
    }

    /**
     * Muestra todos los usuarios registrados en el sistema.
     *
     * @param users Lista de usuarios del sistema.
     */
    private static void showUsers(ArrayList<User> users) {
        // Imprime el nombre de cada usuario precedido de un número
        for (int i = 0; i < users.size(); i++) {
            System.out.println((i + 1) + ")- " + users.get(i).getName());
        }
    }

    /**
     * Muestra un mensaje indicando que no hay usuarios en el sistema y que se debe crear uno para jugar.
     */
    private static void cantPlay() {
        System.out.println("No has creado ningun usuario, debes de crear un usuario para jugar");
    }

    /**
     * Solicita al usuario que seleccione una opción para realizar una acción en el menú principal.
     *
     * @return La opción seleccionada por el usuario.
     */
    private static int getOption() {
        int option;
        System.out.println("Que quieres hacer:\n\t-1) Crear Usuario\n\t-2) Jugar ronda\n\t-3) Mostrar ranking\n\t-0) Salir");
        option = getResponsInt(0, 3); // Solicita la opción seleccionada dentro del rango permitido
        return option;
    }
    /**
     * Solicita al usuario que seleccione las categorías de preguntas con las que desea jugar.
     * Cada categoría tiene un nivel de dificultad.
     *
     * @param numberCategorys Número total de categorías que el usuario debe seleccionar.
     * @return Un array de enteros que contiene las categorías seleccionadas por el usuario.
     */
    private static int[] selectionCategorys(int numberCategorys) {
        int[] Arraycategorys = new int[numberCategorys]; // Array que almacenará las categorías seleccionadas
        int category;
        Arrays.fill(Arraycategorys, 0, numberCategorys, -1); // Inicializa el array con el valor -1
        System.out.println("ATENCIÓN(El nivel basico es equivalente a 1, el intermedio al 2, el avanzado al 3 y el experto al 4)");
        int j = 1;

        // Solicita al usuario seleccionar una categoría, asegurándose de que no se repita
        for (int i = 0; i < Arraycategorys.length; i++) {
            System.out.println("Dime la dificultad numero " + j + " que quieres que salga");
            category = getResponsInt(1, 4) - 1; // Lee la dificultad elegida (ajustada para que comience desde 0)
            if (validationCategory(category, Arraycategorys)) {
                Arraycategorys[i] = category; // Añade la categoría si es válida
                j++;
            } else {
                System.out.println("Esa categoria seleccionada ya existe");
                i--; // Decrementa el índice para repetir la selección en caso de categoría repetida
            }
        }
        System.out.println("¡¡¡Perfectoo!!!");
        return Arraycategorys;
    }

    /**
     * Solicita al usuario cuántas preguntas desea responder en cada categoría seleccionada.
     *
     * @param numberQuestions Número total de preguntas que el usuario desea responder.
     * @param numberCategorys Número de categorías seleccionadas.
     * @return Un array que contiene el número de preguntas que el usuario desea de cada categoría.
     */
    private static int[] selectionNumberQuestionsCategory(int numberQuestions, int numberCategorys) {
        int[] Arraynumberofquestions = new int[numberCategorys]; // Array para almacenar el número de preguntas por categoría
        int totalQuestions = 0;
        int Categorys = 1;
        int Questions;
        boolean finish = false;
        boolean valide;

        // Si solo hay una categoría, asigna todas las preguntas a esa categoría
        if (numberCategorys == 1) {
            Arraynumberofquestions[0] = numberQuestions;
        } else {
            // Solicita al usuario el número de preguntas para cada categoría hasta que coincidan con el total
            do {
                for (int i = 0; i < numberCategorys; i++) {
                    do {
                        valide = false;
                        System.out.println("Cuantas preguntas quieres de la categoria numero " + Categorys + " que has elejido?");
                        Questions = readInt(); // Lee el número de preguntas deseadas
                        if (Questions > 0) {
                            totalQuestions += Questions; // Incrementa el total de preguntas seleccionadas
                            Arraynumberofquestions[Categorys - 1] = Questions; // Asigna las preguntas a la categoría correspondiente
                            Categorys++;
                            valide = true;
                        } else {
                            System.out.println("Tienes que escojer minimo una pregunta");
                        }
                    } while (!valide);
                }

                // Verifica si el total de preguntas seleccionadas coincide con el total deseado
                if (totalQuestions != numberQuestions) {
                    System.out.println("No puedes escojer mas ni menos preguntas de las que tienes preseleccionadas que son " + numberQuestions);
                    Arrays.fill(Arraynumberofquestions, 0, Categorys - 1, 0); // Reinicia el array si no coincide el total
                    Categorys = 1;
                    totalQuestions = 0; // Reinicia el total de preguntas seleccionadas
                } else {
                    finish = true;
                }
            } while (!finish);
        }

        System.out.println("Todo listo, ¡¡¡Que empiece el quizz!!!");
        System.out.println("Tomate el tiempo que quieras para responder las preguntas, al final del quizz te daremos una puntuacion" +
                " y recuerda que si acomulas una racha tu puntuacion se vera potenciada.\n ¡¡¡MUCHA SUERTE!!!  ");
        return Arraynumberofquestions;
    }

    /**
     * Valida que una categoría no haya sido seleccionada previamente.
     *
     * @param category Categoría seleccionada por el usuario.
     * @param Arraycategorys Array que contiene las categorías ya seleccionadas.
     * @return true si la categoría es válida, false si ya ha sido seleccionada.
     */
    private static boolean validationCategory(int category, int[] Arraycategorys) {
        boolean valide = true;
        int i = 0;

        // Recorre el array para verificar que la categoría no se haya seleccionado previamente
        while (i < Arraycategorys.length && valide) {
            if (Arraycategorys[i] == category) {
                valide = false; // Si la categoría ya está en el array, la marca como no válida
            } else {
                i++;
            }
        }
        return valide;
    }

    /**
     * Ejecuta una ronda del cuestionario, mostrando preguntas y recibiendo respuestas del usuario.
     *
     * @param questions Lista de preguntas organizadas por categorías.
     * @param answeres Lista de respuestas asociadas a las preguntas.
     * @param numberOfQuestions Array que contiene el número de preguntas por categoría que quedan por responder.
     * @param category Array de categorías seleccionadas.
     * @param questionsPrinted Array que contiene las preguntas ya impresas.
     * @param categoryElections Array de las categorías seleccionadas para el juego.
     * @param numberOfQuestionGame Array que contiene el número total de preguntas jugadas en el juego.
     * @return La respuesta seleccionada por el usuario.
     */
    private static int printQuestionAndTakeAnswere(ArrayList<ArrayList<String>> questions, ArrayList<ArrayList<ArrayList<String>>> answeres,
                                                   int[] numberOfQuestions, int[] category, int[] questionsPrinted,
                                                   int[] categoryElections, int[] numberOfQuestionGame) {
        int questionRandom, answer;
        int[] answeresPrint = new int[4]; // Array para almacenar las respuestas impresas
        int[] numberAnsw = {0};
        int randomAnswere;
        Arrays.fill(answeresPrint, 0, 4, -1); // Inicializa las respuestas impresas con el valor -1

        // Verifica si la ronda es válida
        if (valideRound(category, numberOfQuestions)) {
            // Selecciona una pregunta aleatoria
            do {
                questionRandom = (int) (Math.random() * 20);
            } while (!valiValueInArray(questionsPrinted, questionRandom));

            questionsPrinted[questionRandom] = questionRandom; // Marca la pregunta como impresa
            System.out.println("( " + numberOfQuestionGame[0] + " ) " + questions.get(categoryElections[category[0]]).get(questionRandom));
            numberOfQuestions[category[0]]--; // Decrementa el número de preguntas restantes
            numberOfQuestionGame[0]++;

            // Imprime las respuestas de forma aleatoria
            for (int i = 0; i < 4; i++) {
                do {
                    randomAnswere = (int) (Math.random() * 4);
                } while (!validationRandomAnswere(randomAnswere, answeresPrint, numberAnsw));
                System.out.println((i + 1) + "· " + answeres.get(categoryElections[category[0]]).get(questionRandom).get(randomAnswere));
            }

            System.out.println("Escoje la respuesta que creas correcta del 1 al 4");
            answer = getResponsInt(1, 4) - 1;
            answer = answeresPrint[answer]; // Obtiene el id de la respuesta seleccionada
        } else {
            answer = -1; // Si la ronda no es válida, devuelve -1
        }
        return answer;
    }
    /**
     * Busca un valor en un array y determina si no ha sido previamente seleccionado.
     *
     * @param array El array en el que se busca el valor.
     * @param value El valor que se desea verificar.
     * @return true si el valor no está en el array, false si ya está presente.
     */
    private static boolean valiValueInArray(int[] array, int value) {
        boolean valide = true;
        int i = 0;
        while (i < array.length && valide) {
            if (array[i] == value) {
                valide = false; // El valor ya está presente
            } else {
                i++;
            }
        }
        return valide;
    }

    /**
     * Valida que la respuesta seleccionada no haya sido mostrada previamente.
     *
     * @param randomAnswere Respuesta seleccionada aleatoriamente.
     * @param answeresPrint Array que contiene las respuestas ya impresas.
     * @param numberAnsw Array que cuenta el número de respuestas mostradas.
     * @return true si la respuesta no ha sido impresa previamente, false si ya se mostró.
     */
    private static boolean validationRandomAnswere(int randomAnswere, int[] answeresPrint, int[] numberAnsw) {
        boolean valide = true;
        int i = 0;

        // Verifica si la respuesta ya fue impresa
        while (i < answeresPrint.length && valide) {
            if (answeresPrint[i] == randomAnswere) {
                valide = false; // La respuesta ya fue impresa
            } else {
                i++;
            }
        }

        if (valide) {
            // Si es válida, agrega la respuesta al array de respuestas impresas y aumenta el contador
            answeresPrint[numberAnsw[0]] = randomAnswere;
            numberAnsw[0] += 1;
        }
        return valide;
    }

    /**
     * Verifica si la respuesta del usuario es correcta y actualiza la puntuación y la racha.
     *
     * @param respons La respuesta seleccionada por el usuario.
     * @param points La puntuación actual del usuario.
     * @param streak Array que contiene la racha de respuestas correctas.
     * @param categoryElections Array de las categorías seleccionadas para el juego.
     * @param category Categoría actual del juego.
     * @return La nueva puntuación después de verificar la respuesta.
     */
    private static double checkAnswere(int respons, double points, double[] streak, int[] categoryElections, int[] category) {
        // Todas las respuestas correctas tienen el índice 0
        if (respons == 0) {
            System.out.println("¡¡¡Has acertado!!!");
            // Asigna puntos según la categoría y la racha de aciertos
            switch (categoryElections[category[0]]) {
                case 0:
                    points += 250 * streak[0];
                    streak[0] += 0.1;
                    break;
                case 1:
                    points += 350 * streak[0];
                    streak[0] += 0.15;
                    break;
                case 2:
                    points += 450 * streak[0];
                    streak[0] += 0.2;
                    break;
                case 3:
                    points += 550 * streak[0];
                    streak[0] += 0.25;
                    break;
            }
        } else {
            // Respuesta incorrecta, reinicia la racha
            streak[0] = 1;
            System.out.println("Has fallado...");
        }
        return points;
    }

    /**
     * Verifica si aún quedan rondas por jugar en el cuestionario.
     *
     * @param category Categoría actual.
     * @param numberOfQuestions Array con el número de preguntas restantes en cada categoría.
     * @return true si se puede jugar una ronda más, false si no hay más preguntas.
     */
    private static boolean valideRound(int[] category, int[] numberOfQuestions) {
        boolean valide;
        // Si no hay más preguntas en la categoría actual, pasa a la siguiente
        if (numberOfQuestions[category[0]] == 0) {
            category[0] += 1;
            if (category[0] > numberOfQuestions.length - 1) {
                valide = false; // No quedan más categorías
            } else {
                valide = true; // Hay más categorías por jugar
            }
        } else {
            valide = true; // Quedan preguntas en la categoría actual
        }
        return valide;
    }

    /**
     * Muestra el resultado final del cuestionario, incluyendo la puntuación total.
     *
     * @param points Puntuación total obtenida por el usuario.
     */
    private static void result(double points) {
        System.out.println("Felicidades, has acabado el quizz, tu puntuacion total ha sido de " + points);
    }

    /**
     * Carga todas las preguntas del cuestionario desde un archivo de texto.
     *
     * @return Un ArrayList de ArrayList que contiene todas las preguntas organizadas por categorías.
     */
    private static ArrayList<ArrayList<String>> fillOutQuestions() {
        ArrayList<ArrayList<String>> arrayGlobal = new ArrayList<>();
        ArrayList<String> array = new ArrayList<>();
        Path path = Paths.get("src/data/Questions"); // Ruta del archivo de preguntas
        List<String> listQuestions = null;

        // Lee el archivo de preguntas
        try {
            listQuestions = Files.readAllLines(path);
        } catch (IOException e) {
            System.out.println("No se ha cargado el fichero de preguntas");
        }

        // Añade cada pregunta a su categoría correspondiente
        for (int i = 0; i < listQuestions.size(); i++) {
            if (listQuestions.get(i).equals("//")) {
                arrayGlobal.add(array);
                array = new ArrayList<>();
            } else {
                array.add(listQuestions.get(i));
            }
        }
        arrayGlobal.add(array); // Añade la última categoría
        return arrayGlobal;
    }

    /**
     * Carga todas las respuestas del cuestionario desde un archivo de texto.
     *
     * @return Un ArrayList de ArrayList de ArrayList que contiene todas las respuestas organizadas por categorías y preguntas.
     */
    private static ArrayList<ArrayList<ArrayList<String>>> fillOutAnsweres() {
        ArrayList<ArrayList<ArrayList<String>>> arrayTotal = new ArrayList<>();
        ArrayList<ArrayList<String>> arrayAnswereAnwereDifi = new ArrayList<>();
        ArrayList<String> arrayListAnswereQuest = new ArrayList<>();
        Path path = Paths.get("src/data/Answeres"); // Ruta del archivo de respuestas
        List<String> listAnsweres = null;

        // Lee el archivo de respuestas
        try {
            listAnsweres = Files.readAllLines(path);
        } catch (IOException e) {
            System.out.println("No se ha cargado el fichero de respuestas");
        }

        // Añade cada respuesta a la pregunta y categoría correspondiente
        for (int i = 0; i < listAnsweres.size(); i++) {
            if (listAnsweres.get(i).equals("//")) {
                arrayTotal.add(arrayAnswereAnwereDifi);
                arrayAnswereAnwereDifi = new ArrayList<>();
            } else if (listAnsweres.get(i).equals("/")) {
                arrayAnswereAnwereDifi.add(arrayListAnswereQuest);
                arrayListAnswereQuest = new ArrayList<>();
            } else {
                arrayListAnswereQuest.add(listAnsweres.get(i));
            }
        }
        arrayAnswereAnwereDifi.add(arrayListAnswereQuest); // Añade la última categoría
        arrayTotal.add(arrayAnswereAnwereDifi);
        return arrayTotal;
    }
    /**
     * Pregunta al usuario cuántas preguntas quiere responder en el cuestionario.
     *
     * @return El número de preguntas elegido por el usuario, dentro del rango de 5 a 20.
     */
    private static int askNumberQuestion() {
        int numberQuestions;
        System.out.println("Hola muy buenas, bienvenido al quiz de Astrofisica, cuantas preguntas quieres hacer?");
        numberQuestions = getResponsInt(5, 20); // Pide una cantidad de preguntas entre 5 y 20
        return numberQuestions;
    }

    /**
     * Lee un número entero de la entrada, gestionando posibles excepciones de tipo InputMismatchException.
     *
     * @return El número entero introducido por el usuario.
     */
    private static int readInt() {
        boolean valide;
        int number = 0;
        do {
            valide = true;
            try {
                number = Teclat.llegirInt(); // Lee un entero desde el teclado
            } catch (InputMismatchException e) {
                System.out.println("El valor introducido no es un numero"); // Maneja la excepción si el valor no es un entero
                valide = false;
            }
        } while (!valide); // Repite hasta que el usuario introduzca un número válido
        return number;
    }

    /**
     * Pregunta al usuario cuántas categorías de dificultad quiere incluir en el cuestionario.
     *
     * @return El número de categorías elegido por el usuario, dentro del rango de 1 a 4.
     */
    private static int askNumberCategorys() {
        int numberCategorys;
        System.out.println("Perfecto, tenemos diferentes divisiones dependiendo de su dificultad: básica, intermedia, avanzada y experta. " +
                "¿Cuántas divisiones quieres?");
        numberCategorys = getResponsInt(1, 4); // Pide una cantidad de categorías entre 1 y 4
        return numberCategorys;
    }

    /**
     * Valida que la respuesta introducida por el usuario esté dentro de un rango permitido.
     *
     * @param min El valor mínimo permitido.
     * @param max El valor máximo permitido.
     * @return El número introducido por el usuario dentro del rango especificado.
     */
    private static int getResponsInt(int min, int max) {
        int number;
        number = readInt(); // Lee un número del usuario
        while (number < min || number > max) { // Verifica que esté dentro del rango permitido
            System.out.println("El valor tiene que estar entre " + min + " y " + max);
            number = readInt(); // Pide un nuevo número si no está dentro del rango
        }
        return number;
    }
}

