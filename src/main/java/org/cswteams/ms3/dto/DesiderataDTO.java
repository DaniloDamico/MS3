package org.cswteams.ms3.dto;

import lombok.Getter;
import org.cswteams.ms3.enums.TimeSlot;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DesiderataDTO {

    private Long idDesiderata;
    private int giorno;
    private int mese;
    private int anno;
    private List<TimeSlot> tipologieTurni;

    public DesiderataDTO(int giorno, int mese, int anno, List<TimeSlot> tipologieTurni) {
        this.giorno = giorno;
        this.mese = mese;
        this.anno = anno;
        this.tipologieTurni = tipologieTurni;
    }


    public DesiderataDTO(int giorno, int mese, int anno) {
        this.giorno = giorno;
        this.mese = mese;
        this.anno = anno;
        this.tipologieTurni = new ArrayList<>();
    }

    public DesiderataDTO(Long idDesiderata, int giorno, int mese, int anno, List<TimeSlot> tipologieTurni) {
        this(giorno, mese, anno, tipologieTurni);
        this.idDesiderata = idDesiderata;
    }

    public DesiderataDTO(){}
}
