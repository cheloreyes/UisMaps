package com.proyecto.uis.uismaps;

import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by cheloreyes on 21/03/15.
 */
public class Thesaurus {

    private final String result;
    private String[] keys = {"portería carrera 27", "Laboratorios Pesados", "Teatro al aire libre Jose Antonio Galan",
            "Auditorio Luis A. Calvo", "Laboratorios Livianos", "Aula Máxima de Física", "Jorge Bautista Vesga",
            "Ciencias Humanas", "Daniel Casas", "Federico Mamitza Bayer", "CENTIC"};

    private String[] articles = {"a","al","como","cómo", "llegar", "quiero", "ir", "la", "el"};

    private String[] lp = {"facultad de físico mecánicas", "facultad", "escuela de sistemas", "escuela de civil",
            "facultad de física mecánica", "físico mecánica", "pesados", "lp"};
    private String[] luisA = {"Burladero", "luisa", "luis", "luis A", "luis  calvo", "calvo", "luisa calvo",
            "auditorio luis calvo", "auditorio luis"};
    private String[] entry = {"entrada", "ingreso", "entrada principal"};
    private String[] ll = {"livianos", "ll", "l l", "escuelda de matemáticas", "escuela de biología", "escuela de química", "escuela de física"};
    private String[] gallera = {"gallera"};
    private String[] matadero = {"matadero"};
    private String[] jorgeB = {"escuela de petroleos", "escuela de metalúrgica", "escuela de geología"};
    private String[] humanas = {"escuela de economía", "escuela de educación", "escuela de historia", "escuela de idiomas", "escuela de filosofía",
            "escuela de derecho", "escuela de economía", "escuela de tabajo social", "ciencias"};
    private String[] danielC = {"escuela de arte", "escuela de música", "escuela de música y arte"};
    private String[] diseño = {"Federico Mamitza Bayer", "mamitza", "escuela de diseño industrial", "escuela de diseño", "diseño", "diseño industrial"};
    private String[] centic = {"centic", "edificio inteligente"};

    private Dictionary <String, String[]> synonyms;


    public Thesaurus(String speech) {
        synonyms = new Hashtable<>();
        synonyms.put(keys[0], entry);
        synonyms.put(keys[1], lp);
        synonyms.put(keys[2], gallera);
        synonyms.put(keys[3], luisA);
        synonyms.put(keys[4], ll);
        synonyms.put(keys[5], matadero);
        synonyms.put(keys[6], jorgeB);
        synonyms.put(keys[7], humanas);
        synonyms.put(keys[8], danielC);
        synonyms.put(keys[9], diseño);
        synonyms.put(keys[10], centic);

        Log.v("Thesaurus", "Basic Speech: " + speech);
        speech = fixSentence(speech);
        Log.v("Thesaurus", "Fix Speech: " + speech);
        speech = replaceSynonyms(speech);
        Log.v("Thesaurus", "Sinonimos: " + speech);
        result = speech;
    }

    private String fixSentence(String sentence) {
        for (int i = 0; i < articles.length; i++) {
            sentence = sentence.replaceAll("\\b" + articles[i] + "\\b", "");
        }
        sentence = sentence.trim();
        return sentence;
    }

    private String replaceSynonyms(String sentence) {
        for(int i = 0; i < synonyms.size(); i++) {
            for (int j = 0; j < synonyms.get(keys[i]).length; j++){
                if (sentence.equalsIgnoreCase(synonyms.get(keys[i])[j])) {
                    sentence = keys[i];
                    //Log.v("Thesaurus", "Conicide con: " + synonyms.get(keys[i])[j]);
                }
                //Log.v("Thesaurus", "No conicide con: " + synonyms.get(keys[i])[j]);
            }
        }
        return sentence;
    }

    public String getResult() {
        return result;
    }
}

