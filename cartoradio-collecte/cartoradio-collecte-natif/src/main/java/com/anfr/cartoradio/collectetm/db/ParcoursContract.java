package com.anfr.cartoradio.collectetm.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.BaseColumns;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;

public final class ParcoursContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private ParcoursContract() {
    }

    /* Inner class that defines the table contents */
    public static class Parcours implements BaseColumns, Parcelable {
        public static final String TABLE_NAME = "parcours";
        public static final String COLUMN_NAME_DATE_DEBUT = "date_debut";
        public static final String COLUMN_NAME_DATE_FIN = "date_fin";
        public static final String COLUMN_NAME_TITRE = "titre";
        public static final String COLUMN_NAME_SYNCHRONISE = "synchronise";

        private UUID id;
        private Integer dateDebut;
        private Integer dateFin;
        private String titre;
        private boolean synchronise;

        public Parcours() {
        }

        protected Parcours(Parcel in) {
            id = UUID.fromString(in.readString());
            dateDebut = in.readInt();
            int i = in.readInt();
            dateFin = i == 0 ? null : i;
            titre = in.readString();
            synchronise = in.readByte() != 0;
        }

        public static final Creator<Parcours> CREATOR = new Creator<Parcours>() {
            @Override
            public Parcours createFromParcel(Parcel in) {
                return new Parcours(in);
            }

            @Override
            public Parcours[] newArray(int size) {
                return new Parcours[size];
            }
        };

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public Integer getDateDebut() {
            return dateDebut;
        }

        public void setDateDebut(Integer dateDebut) {
            this.dateDebut = dateDebut;
        }

        public Integer getDateFin() {
            return dateFin;
        }

        public void setDateFin(Integer dateFin) {
            this.dateFin = dateFin;
        }

        public String getTitre() {
            return titre;
        }

        public void setTitre(String titre) {
            this.titre = titre;
        }

        public boolean isSynchronise() {
            return synchronise;
        }

        public void setSynchronise(boolean synchronise) {
            this.synchronise = synchronise;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(id.toString());
            parcel.writeInt(dateDebut);
            parcel.writeInt(dateFin == null ? 0 : dateFin);
            parcel.writeString(titre);
            parcel.writeByte((byte) (synchronise ? 1 : 0));
        }
    }

    //Récupération de tous les parcours se trouvent dans la base de donnée SQLite et la retoune sous forme de liste
    public static Callable<List<Parcours>> list(final SQLiteDatabase db) {
        return new Callable<List<Parcours>>() {
            @Override
            public List<Parcours> call() {
                Cursor c = db.query(Parcours.TABLE_NAME,
                        new String[]{Parcours._ID, Parcours.COLUMN_NAME_DATE_DEBUT, Parcours.COLUMN_NAME_DATE_FIN, Parcours.COLUMN_NAME_TITRE, Parcours.COLUMN_NAME_SYNCHRONISE},
                        null, null, null, null, Parcours.COLUMN_NAME_DATE_DEBUT + " desc", null);
                List<Parcours> res = new ArrayList<>();
                while (c.moveToNext()) {
                    Parcours parcours = new Parcours();
                    parcours.setId(UUID.fromString(c.getString(0)));
                    parcours.setDateDebut(c.isNull(1) ? null : c.getInt(1));
                    parcours.setDateFin(c.isNull(2) ? null : c.getInt(2));
                    parcours.setTitre(c.getString(3));
                    parcours.setSynchronise(c.isNull(4) ? false : c.getInt(4) == 1);
                    res.add(parcours);
                }
                c.close();
                return res;
            }
        };
    }

    //Récupération du parcours courant se trouvant dans la base de données SQLite
    public static Callable<Parcours> getCurrent(final SQLiteDatabase db) {
        return new Callable<Parcours>() {
            @Override
            public Parcours call() {
                Cursor c = db.query(Parcours.TABLE_NAME,
                        new String[]{Parcours._ID, Parcours.COLUMN_NAME_DATE_DEBUT, Parcours.COLUMN_NAME_DATE_FIN, Parcours.COLUMN_NAME_TITRE, Parcours.COLUMN_NAME_SYNCHRONISE},
                        Parcours.COLUMN_NAME_DATE_FIN + " is null", null, null, null, null, null);
                Parcours parcours = null;
                while (c.moveToNext()) {
                    parcours = new Parcours();
                    parcours.setId(UUID.fromString(c.getString(0)));
                    parcours.setDateDebut(c.isNull(1) ? null : c.getInt(1));
                    parcours.setDateFin(c.isNull(2) ? null : c.getInt(2));
                    parcours.setTitre(c.getString(3));
                    parcours.setSynchronise(c.isNull(4) ? false : c.getInt(4) == 1);
                }
                c.close();
                return parcours;
            }
        };
    }


    public static void insert(final SQLiteDatabase db, String titre) {
        ContentValues values = new ContentValues();
        Long tsLong = System.currentTimeMillis() / 1000;

        values.put(Parcours._ID, UUID.randomUUID().toString());
        values.put(Parcours.COLUMN_NAME_SYNCHRONISE, 0);
        values.put(Parcours.COLUMN_NAME_DATE_DEBUT, tsLong);
        values.putNull(Parcours.COLUMN_NAME_DATE_FIN);
        values.put(Parcours.COLUMN_NAME_TITRE, titre);

        db.insert(Parcours.TABLE_NAME, null, values);
    }

    public static void close(final SQLiteDatabase db) {
        ContentValues values = new ContentValues();
        Long tsLong = System.currentTimeMillis() / 1000;

        values.put(Parcours.COLUMN_NAME_DATE_FIN, tsLong);

        db.update(Parcours.TABLE_NAME, values, Parcours.COLUMN_NAME_DATE_FIN + " is null", null);
    }

    public static void markSynchronised(final SQLiteDatabase db, final UUID parcoursId) {
        ContentValues values = new ContentValues();

        values.put(Parcours.COLUMN_NAME_SYNCHRONISE, true);

        db.update(Parcours.TABLE_NAME, values, Parcours._ID + " is ?", new String[]{parcoursId.toString()});
    }

}

