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
            "Ciencias Humanas", "Daniel Casas", "Federico Mamitza Bayer", "CENTIC", "Planta Telefonica", "Canchas Multiples", "CICELPA CEIAM"};

    private static final String[] articles = {"a","al","como","cómo", "llegar", "quiero", "ir", "la", "el", "edificio", "las"};

    private static final String[] lp = {"facultad de físico mecánicas", "facultad", "escuela de sistemas", "escuela de civil",
                                        "facultad de física mecánica", "físico mecánica", "pesados", "lp"};
    private String[] luisA = {"Burladero", "luisa", "luis", "luis A", "luis  calvo", "calvo", "luisa calvo",
                              "auditorio luis calvo", "auditorio luis"};
    private static final String[] entry = {"entrada", "ingreso", "entrada principal"};
    private static final String[] ll = {"livianos", "ll", "l l", "escuelda de matemáticas", "escuela de biología", "escuela de química", "escuela de física"};
    private static final String[] gallera = {"gallera"};
    private static final String[] matadero = {"matadero"};
    private static final String[] jorgeB = {"escuela de petroleos", "escuela de metalúrgica", "escuela de geología", "de petróleos", "petróleos"};
    private static final String[] humanas = {"escuela de economía", "escuela de educación", "escuela de historia", "escuela de idiomas", "escuela de filosofía",
                                             "escuela de derecho", "escuela de economía", "escuela de tabajo social", "ciencias"};
    private static final String[] danielC = {"escuela de arte", "escuela de música", "escuela de música y arte"};
    private static final String[] diseño = {"Federico Mamitza Bayer", "mamitza", "escuela de diseño industrial", "escuela de diseño", "diseño", "diseño industrial"};
    private static final String[] centic = {"centic", "edificio inteligente", "sentir", "inteligente"};
    private static final String[] coffee = {"Cafeteria central", "Cafeteria", "Cafetería", "central", "cafetería central"};
    private static final String[] canchas = {"Canchas Multiples", "canchas", "deporte", "cancha de fútbol"};
    private static final String[] cicelpa = {"Centro de Estudios e Investigaciones Ambientales", "Investigaciones Ambientales", "centro de investigaciones ambientales", "centro investigaciones ambientales"};

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
        synonyms.put(KEYS[0], entry);
        synonyms.put(KEYS[1], lp);
        synonyms.put(KEYS[2], gallera);
        synonyms.put(KEYS[3], luisA);
        synonyms.put(KEYS[4], ll);
        synonyms.put(KEYS[5], matadero);
        synonyms.put(KEYS[6], jorgeB);
        synonyms.put(KEYS[7], humanas);
        synonyms.put(KEYS[8], danielC);
        synonyms.put(KEYS[9], diseño);
        synonyms.put(KEYS[10], centic);
        synonyms.put(KEYS[11], coffee);
        synonyms.put(KEYS[12], canchas);
        synonyms.put(KEYS[13], cicelpa);

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
        for (int i = 0; i < articles.length; i++) {
            sentence = sentence.replaceAll("\\b" + articles[i] + "\\b", "");
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

