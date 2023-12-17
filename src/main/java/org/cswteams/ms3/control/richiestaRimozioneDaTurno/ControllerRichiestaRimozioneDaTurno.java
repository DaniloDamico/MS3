package org.cswteams.ms3.control.richiestaRimozioneDaTurno;

import org.cswteams.ms3.control.assegnazioneTurni.IControllerAssegnazioneTurni;
import org.cswteams.ms3.control.utils.MappaRichiestaRimozioneDaTurno;
import org.cswteams.ms3.dao.AssegnazioneTurnoDao;
import org.cswteams.ms3.dao.RichiestaRimozioneDaTurnoDao;
import org.cswteams.ms3.dao.UtenteDao;
import org.cswteams.ms3.dto.RichiestaRimozioneDaTurnoDTO;
import org.cswteams.ms3.entity.AssegnazioneTurno;
import org.cswteams.ms3.entity.RichiestaRimozioneDaTurno;
import org.cswteams.ms3.entity.Utente;
import org.cswteams.ms3.exception.AssegnazioneTurnoException;
import org.cswteams.ms3.exception.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@Service
public class ControllerRichiestaRimozioneDaTurno implements IControllerRichiestaRimozioneDaTurno {

    @Autowired
    private RichiestaRimozioneDaTurnoDao richiestaRimozioneDaTurnoDao;

    @Autowired
    private IControllerAssegnazioneTurni controllerAssegnazioneTurni;

    @Autowired
    private AssegnazioneTurnoDao assegnazioneTurnoDao;

    @Autowired
    private UtenteDao utenteDao;

    @Override
    public RichiestaRimozioneDaTurno creaRichiestaRimozioneDaTurno(@NotNull RichiestaRimozioneDaTurnoDTO richiestaRimozioneDaTurnoDTO) throws DatabaseException, AssegnazioneTurnoException {
        // 1. ricerca AssegnazioneTurno --------------------------------------------------------------
        Long assegnazioneTurnoId = richiestaRimozioneDaTurnoDTO.getIdAssegnazioneTurno();
        if (assegnazioneTurnoId == null) {
            throw new DatabaseException("Id AssegnazioneTurno non valida");
        }
        Optional<AssegnazioneTurno> assegnazioneTurno = assegnazioneTurnoDao.findById(assegnazioneTurnoId);
        if (assegnazioneTurno.isEmpty()) {
            throw new DatabaseException("AssegnazioneTurno non trovata per id = " + assegnazioneTurnoId);
        }

        // 2. ricerca Utente -------------------------------------------------------------------------
        Long utenteRichiedenteId = richiestaRimozioneDaTurnoDTO.getIdUtenteRichiedente();
        if (utenteRichiedenteId == null) {
            throw new DatabaseException("Id Utente non valido");
        }
        Optional<Utente> utenteRichiedente = utenteDao.findById(utenteRichiedenteId);
        if (utenteRichiedente.isEmpty()) {
            throw new DatabaseException("AssegnazioneTurno non trovata per id = " + assegnazioneTurnoId);
        }

        // 3. chiamata a metodo interno -----------------------------------------------------------
        return this._creaRichiestaRimozioneDaTurno(assegnazioneTurno.get(), utenteRichiedente.get(), richiestaRimozioneDaTurnoDTO.getDescrizione());
    }

    @Override
    public RichiestaRimozioneDaTurno _creaRichiestaRimozioneDaTurno(@NotNull AssegnazioneTurno assegnazioneTurno, @NotNull Utente utenteRichiedente, @NotNull String descrizione) throws DatabaseException, AssegnazioneTurnoException {
        if (!richiestaRimozioneDaTurnoDao.findAllByAssegnazioneTurnoIdAndUtenteId(assegnazioneTurno.getId(), utenteRichiedente.getId()).isEmpty()) {
            throw new DatabaseException("Esiste già una richiesta di rimozione da turno assegnato per l'utente " + utenteRichiedente + " per il la assegnazione " + assegnazioneTurno);
        }
        if (!assegnazioneTurno.isAllocated(utenteRichiedente) && !assegnazioneTurno.isReserve(utenteRichiedente)) {
            throw new AssegnazioneTurnoException("L'utente " + utenteRichiedente + " non risulta essere coinvolto nella assegnazione turno " + assegnazioneTurno);
        }
        RichiestaRimozioneDaTurno richiestaRimozioneDaTurno = new RichiestaRimozioneDaTurno(assegnazioneTurno, utenteRichiedente, descrizione);

        richiestaRimozioneDaTurnoDao.saveAndFlush(richiestaRimozioneDaTurno);
        return richiestaRimozioneDaTurno;
    }

    @Override
    public Set<RichiestaRimozioneDaTurnoDTO> leggiRichiesteRimozioneDaTurno() {
        return MappaRichiestaRimozioneDaTurno.richiestaRimozioneDaTurnoEntitytoDTO(richiestaRimozioneDaTurnoDao.findAll());
    }

    @Override
    public Set<RichiestaRimozioneDaTurnoDTO> leggiRichiesteRimozioneDaTurnoPendenti() {
        return MappaRichiestaRimozioneDaTurno.richiestaRimozioneDaTurnoEntitytoDTO(richiestaRimozioneDaTurnoDao.findAllPending());
    }

    @Override
    @Transactional
    public Set<RichiestaRimozioneDaTurnoDTO> leggiRichiesteRimozioneDaTurnoPerUtente(Long utenteId) {
        return MappaRichiestaRimozioneDaTurno.richiestaRimozioneDaTurnoEntitytoDTO(richiestaRimozioneDaTurnoDao.findAllByUser(utenteId));
    }

    @Override
    public Optional<RichiestaRimozioneDaTurnoDTO> leggiRichiestaRimozioneDaTurno(Long idRichiesta) {
        Optional<RichiestaRimozioneDaTurno> r = richiestaRimozioneDaTurnoDao.findById(idRichiesta);
        return r.map(MappaRichiestaRimozioneDaTurno::richiestaRimozioneDaTurnoToDTO);
    }

    @Override
    public RichiestaRimozioneDaTurno risolviRichiestaRimozioneDaTurno(Long idRichiesta, boolean esito) throws DatabaseException, AssegnazioneTurnoException {
        Optional<RichiestaRimozioneDaTurno> r = richiestaRimozioneDaTurnoDao.findById(idRichiesta);
        if (r.isEmpty()) {
            throw new DatabaseException("RichiestaRimozioneDaTurno non trovata per id = " + idRichiesta);
        }
        if (r.get().isEsaminata()) {
            throw new DatabaseException("La RichiestaRimozioneDaTurno avente id = " + idRichiesta + " risulta essere già stata esaminata");
        }
        if (esito) {
            AssegnazioneTurno assegnazioneTurno = r.get().getAssegnazioneTurno();
            Utente richiedente = r.get().getUtenteRichiedente();
            Utente sostituto = r.get().getUtenteSostituto();
            try {
                controllerAssegnazioneTurni.sostituisciUtenteAssegnato(assegnazioneTurno, richiedente, sostituto);
            } catch (AssegnazioneTurnoException e) {
                throw new AssegnazioneTurnoException("Impossibile sostituire.");
            }
            r.get().setEsito(true);
            r.get().setUtenteSostituto(sostituto);
        } else {
            r.get().setEsito(false);
        }
        r.get().setEsaminata(true);
        richiestaRimozioneDaTurnoDao.saveAndFlush(r.get());
        return r.get();
    }

    @Override
    @Transactional
    public RichiestaRimozioneDaTurno caricaAllegato(@NotNull Long idRichiestaRimozioneDaTurno, @NotNull MultipartFile allegato) throws IOException, DatabaseException {
        Optional<RichiestaRimozioneDaTurno> r = richiestaRimozioneDaTurnoDao.findById(idRichiestaRimozioneDaTurno);
        if (r.isEmpty()) {
            throw new DatabaseException("RichiestaRimozioneDaTurno non trovata per id = " + idRichiestaRimozioneDaTurno);
        }
        r.get().setAllegato(allegato.getBytes());
        richiestaRimozioneDaTurnoDao.saveAndFlush(r.get());
        return r.get();
    }
}
