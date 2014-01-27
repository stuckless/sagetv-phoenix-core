package sagex.phoenix.progress;

import java.util.Calendar;
import java.util.Date;

public class TrackedItem<T> {
    private Date dateTime;
    private T item;
    private String message;
    private Throwable error;
    
    public TrackedItem(T item) {
        this.dateTime = Calendar.getInstance().getTime();
        this.item =item;
    }

    public TrackedItem(T item, String msg) {
        this.dateTime = Calendar.getInstance().getTime();
        this.message=msg;
        this.item =item;
    }
    
    public TrackedItem(T item, String message, Throwable t) {
        this.dateTime = Calendar.getInstance().getTime();
        this.item =item;
        this.message=message;
        this.error=t;
    }

    /**
     * @return the dateTime
     */
    public Date getDateTime() {
        return dateTime;
    }

    /**
     * @return the item
     */
    public T getItem() {
        return item;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the error
     */
    public Throwable getError() {
        return error;
    }

}
