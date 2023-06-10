package kz.axelrod.finalproject.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "responsible_person")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponsiblePerson {

    @Id
    @SequenceGenerator(name = "responsible_person_seq", sequenceName = "responsible_person_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "responsible_person_seq")
    @Column(name = "responsible_person_id")
    private Long responsiblePersonId;

    @Column(name = "responsible_person_name")
    private String responsiblePersonName;

    @Column(name = "responsible_person_surname")
    private String responsiblePersonSurname;

    @Column(name = "responsible_person_mail")
    private String responsiblePersonMail;

    @Column(name = "responsible_person_phone_number")
    private String responsiblePersonPhoneNumber;
}
