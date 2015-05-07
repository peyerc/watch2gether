package models;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.Date;
import java.util.Set;

/**
 * Created by peyerco on 07.05.15.
 */
@Entity
public class W2GEventMsisdn {

    @Id
    @GeneratedValue
    public Long id;

    public String msisdn;

    public W2GEventMsisdn(String s) {
        this.msisdn = s;
    }
}
