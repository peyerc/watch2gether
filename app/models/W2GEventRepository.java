package models;


import org.springframework.data.repository.CrudRepository;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.Date;
import java.util.List;

/**
 * Created by peyerco on 07.05.15.
 */
@Named
@Singleton
public interface W2GEventRepository extends CrudRepository<W2GEvent, Long> {
    List<W2GEvent> findByTimeBetweenAndDeliveryStatus(Date from, Date to, DeliveryStatus deliveryStatus);
}
