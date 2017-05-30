package com.anfr.cartoradio.collectetm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JThom on 09/05/2017.
 * Correspond à un opérateur téléphonique
 * Note: ça serait fantastique de pouvoir récupérer les cell_id des operateurs, afin de savoir à quel support nous sommes rattachés
 */

public class Operateur {

    //Name (Ex: SFR)
    private String name;

    //Liste des generations (2G, 3G, 4G, ... )
    private List techno = new ArrayList<String>();

    //Liste des systemes( LTE, GSM, etc... )
    private List<systemes> systemes_l = new ArrayList<systemes>();


    public Operateur(String nm)
    {
        this.name = nm;
    }

    public String getName() {return name;}

    public List getTechno() {return techno;}

    public String getTechnoString(){
        String tech = "";
        for(int i=0; i < getTechno().size(); i++)
        {
            tech += getTechno().get(i) + (i+1 != getTechno().size() ? "/" : "");
        }
        return tech;
    }

    //Ajout d'un élement à la liste des systemes de l'operateur(nom, date, generation)
    public void setSystemes_l(String name, String dt, String tech)
    {
        systemes_l.add(new systemes(name, dt, tech));
    }

    //Recuperation de la liste des systemes
    public List getSystemes()
    {
        return systemes_l;
    }

    //Recupération  de la date d'un systeme de la liste (grâce à l'index)
    public String getSystemeDate(int index)
    {
        return  systemes_l.get(index).getDate();
    }

    //Recupération  du nom d'un systeme de la liste (grâce à l'index)
    public String getSystemeName(int index)
    {
        return  systemes_l.get(index).getNames();
    }

    //Recupération  de la techno d'un systeme de la liste (grâce à l'index)
    public String getSystemeTechno(int index){ return systemes_l.get(index).getTechno();}

    //Classe enfant de Operateur( un opérateur possede un ou des systemes)
    public class systemes {

        //Nom du systeme
        private String systeme_name;

        //Date de mise en service
        private String date;

        //Generation du systeme
        private String techno;

        //Bande de fréquence
        private String bdf;

        //Constructeur (Nom, date, generation)
        public systemes(String name, String dt, String techn){
            systeme_name = name;
            date = dt;
            techno = techn;
        }

        //Formatage
        @Override
        public String toString() {
            return systeme_name + " " + date;
        }

        public String getNames()
        {
            return systeme_name;
        }

        public String getDate()
        {
            return date;
        }

        public String getTechno() {return techno;}
    }
}
