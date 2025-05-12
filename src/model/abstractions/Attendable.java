package model.abstractions;

import java.time.LocalDate;
import java.util.Map;

public interface Attendable {
    /**
     * Pažymi lankomumą nurodytai datai
     * @param date Data
     * @param present Ar dalyvauja (true) ar ne (false)
     */
    void markAttendance(LocalDate date, boolean present);

    /**
     * Gauna lankomumo informaciją konkrečiai datai
     * @param date Data
     * @return null jei nėra įrašo, true jei dalyvavo, false jei nedalyvavo
     */
    Boolean getAttendance(LocalDate date);

    /**
     * Grąžina visus lankomumo įrašus
     * @return Lankomumo įrašų map'as (data -> dalyvavimas)
     */
    Map<LocalDate, Boolean> getAllAttendance();
}