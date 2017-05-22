package com.anfr.cartoradio.collectetm;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JThom on 09/05/2017.
 */

public class Operateur {
    private String name;
    private String mise_en_service;
    private List techno = new ArrayList<String>();
    public List<systemes> systemes_l = new ArrayList<systemes>();

    public Operateur(String nm)
    {
        this.name = nm;
    }

    public Operateur(String nm, String mes, List tn)
    {
        this.name = nm;
        this.mise_en_service = mes;
        this.techno = new ArrayList<String>(tn);
    }

    public String getName() {
        return name;
    }

    public String getMise_en_service() {
        return mise_en_service;
    }

    public void setMise_en_service(String mise_en_service) {
        this.mise_en_service = mise_en_service;
    }

    public List getTechno() {
        return techno;
    }

    public void setSystemes_l(String name, String dt)
    {
        systemes_l.add(new systemes(name, dt));
    }

    public List getSystemes()
    {
        return systemes_l;
    }

    public class systemes {
        private String systeme_name;
        private String date;

        public systemes(String name, String dt){
            systeme_name = name;
            date = dt;
        }

        public String getNames()
        {
            return systeme_name;
        }

        public String getDate()
        {
            return date;
        }
    }
}
