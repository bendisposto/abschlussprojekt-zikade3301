package mops.module.wrapper;

import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import mops.module.database.Modul;
import mops.module.database.Veranstaltung;
import mops.module.database.Veranstaltungsform;
import mops.module.database.Zusatzfeld;

@Data
@AllArgsConstructor
public class ModulWrapper {

    @Valid
    Modul modul;
    public List<@Valid Veranstaltung> veranstaltungen;
    public List<@Valid Veranstaltungsform> [] veranstaltungsformen;
    public List<Zusatzfeld> [] zusatzfelder;

}
