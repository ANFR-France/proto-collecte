package com.anfr.cartoradio.collectetm.api;

import java.util.List;

import com.anfr.cartoradio.collectetm.db.MesureContract;
import com.anfr.cartoradio.collectetm.db.ParcoursContract;

public class ParcoursWithMesures {
    private ParcoursContract.Parcours parcours;
    private List<MesureContract.Mesure> mesures;

    public ParcoursContract.Parcours getParcours() {
        return parcours;
    }

    public void setParcours(ParcoursContract.Parcours parcours) {
        this.parcours = parcours;
    }

    public List<MesureContract.Mesure> getMesures() {
        return mesures;
    }

    public void setMesures(List<MesureContract.Mesure> mesures) {
        this.mesures = mesures;
    }
}
