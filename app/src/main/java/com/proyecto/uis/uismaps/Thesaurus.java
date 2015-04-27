package com.proyecto.uis.uismaps;

import android.util.Log;
import java.util.Dictionary;
import java.util.Hashtable;

/**
 * Esta clase funciona de forma similar a un buscador de sinónimos que soporta las búsquedas por voz, intentando hacer que estás sean
 * flexibles para que el usuario pueda usar su lenguaje natural al momento de referirse a los edificios al interior del campus universitario
 * y no necesariamente especificar el nombre de éste, por lo cual se intenta "descifrar" el lugar que se quiere decir.
 *
 * Created by cheloreyes on 21/03/15.
 */
public class Thesaurus {

    // **********************
    // Constants
    // **********************
    private static final String[] KEYS = {"portería carrera 27", "Laboratorios Pesados", "Teatro al aire libre Jose Antonio Galan",
            "Auditorio Luis A. Calvo", "Laboratorios Livianos", "Aula Maxima de Ciencias", "Jorge Bautista Vesga",
            "Ciencias Humanas", "Daniel Casas", "Federico Mamitza Bayer", "CENTIC", "Planta Telefónica", "Canchas Multiples", "CICELPA CEIAM"};

    private static final String[] ARTICLES = {"a","al","como","cómo", "llegar", "quiero", "ir", "la", "el", "edificio", "las"};

    private static final String[] LP = {"facultad de físico mecánicas", "facultad", "escuela de sistemas", "escuela de civil",
                                        "facultad de física mecánica", "físico mecánica", "pesados", "lp"};
    private static final String[] LUIS_A = {"Burladero", "luisa", "luis", "luis A", "luis  calvo", "calvo", "luisa calvo",
                              "auditorio luis calvo", "auditorio luis"};
    private static final String[] ENTRY = {"entrada", "ingreso", "entrada principal", "portería principal", "porteria principal"};
    private static final String[] LL = {"livianos", "ll", "l l", "escuelda de matemáticas", "escuela de biología", "escuela de química", "escuela de física"};
    private static final String[] GALLERA = {"GALLERA"};
    private static final String[] MATADERO = {"MATADERO"};
    private static final String[] JORGE_B = {"escuela de petróleos", "escuela de metalúrgica", "escuela de geología", "de petróleos", "petróleos", "Jorge Bautista"};
    private static final String[] HUMANAS = {"escuela de economía", "escuela de educación", "escuela de historia", "escuela de idiomas", "escuela de filosofía",
                                             "escuela de derecho", "escuela de economía", "escuela de tabajo social", "ciencias"};
    private static final String[] DANIEL_C = {"escuela de arte", "escuela de música", "escuela de música y arte"};
    private static final String[] DISEÑO = {"Federico Mamitza Bayer", "mamitza", "escuela de diseño industrial", "escuela de diseño", "diseño", "diseño industrial"};
    private static final String[] CENTIC = {"centic", "edificio inteligente", "sentir", "inteligente"};
    private static final String[] COFFEE = {"Cafeteria central", "Cafeteria", "Cafetería", "central", "cafetería central"};
    private static final String[] CANCHAS = {"Canchas Multiples", "canchas", "deporte", "cancha de fútbol"};
    private static final String[] CICELPA = {"Centro de Estudios e Investigaciones Ambientales", "Investigaciones Ambientales", "centro de investigaciones ambientales", "centro investigaciones ambientales"};

    // **********************
    // Fields
    // **********************
    private Dictionary <String, String[]> synonyms;
    private String result;

    // **********************
    // Constructor
    // **********************

    /**
     * Al instancer un nuevo objeto de la clase sinónimos, se requiere del texto a "descifrar" el cual es simplificado eliminando los
     * artículos gramaticales, conectores de la oración y es relacionado si coincide con algún significado del arreglo de sinónimos @synonyms
     * @param speech texto a "descifrar".
     */
    public Thesaurus(String speech) {
        synonyms = new Hashtable<>();
        synonyms.put(KEYS[0], ENTRY);
        synonyms.put(KEYS[1], LP);
        synonyms.put(KEYS[2], GALLERA);
        synonyms.put(KEYS[3], LUIS_A);
        synonyms.put(KEYS[4], LL);
        synonyms.put(KEYS[5], MATADERO);
        synonyms.put(KEYS[6], JORGE_B);
        synonyms.put(KEYS[7], HUMANAS);
        synonyms.put(KEYS[8], DANIEL_C);
        synonyms.put(KEYS[9], DISEÑO);
        synonyms.put(KEYS[10], CENTIC);
        synonyms.put(KEYS[11], COFFEE);
        synonyms.put(KEYS[12], CANCHAS);
        synonyms.put(KEYS[13], CICELPA);

        Log.v("Thesaurus", "Basic Speech: " + speech);
        speech = fixSentence(speech);
        Log.v("Thesaurus", "Fix Speech: " + speech);
        speech = replaceSynonyms(speech);
        Log.v("Thesaurus", "Sinonimos: " + speech);
        result = speech;
    }

    // **********************
    // Methods
    // **********************
    /**
     * Simplifica la oración eliminando artículos y conectores gramaticales.
     * @param sentence oración bruta.
     * @return oración simplificada.
     */
    private String fixSentence(String sentence) {
        for (int i = 0; i < ARTICLES.length; i++) {
            sentence = sentence.replaceAll("\\b" + ARTICLES[i] + "\\b", "");
        }
        sentence = sentence.trim();
        return sentence;
    }

    /**
     * Reemplaza el significado si existe el valor en el arreglo @synonyms
     * @param sentence oración simplificada.
     * @return la clave del valor encontrado.
     */
    private String replaceSynonyms(String sentence) {
        for(int i = 0; i < synonyms.size(); i++) {
            for (int j = 0; j < synonyms.get(KEYS[i]).length; j++){
                if (sentence.equalsIgnoreCase(synonyms.get(KEYS[i])[j])) {
                    sentence = KEYS[i];
                }
            }
        }
        return sentence;
    }

    /**
     *
     * @return Lugar de destino.
     */
    public String getResult() {
        return result;
    }
}

