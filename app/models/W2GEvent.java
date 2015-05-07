package models;


import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by peyerco on 07.05.15.
 */
@Entity
public class W2GEvent {
    @Id
    @GeneratedValue
    public Long id;

    public String show;
    public String channel;
    public Date time;
    //public Set<String> msisdns;

}
