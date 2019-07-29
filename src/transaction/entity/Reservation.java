/*
 * Created on 2005-5-17
 *
 */
package transaction.entity;

import transaction.InvalidIndexException;

/**
 * @author RAdmin
 * <p>
 */
public class Reservation extends ResourceItem {
    public static final int RESERVATION_TYPE_FLIGHT = 1;
    public static final int RESERVATION_TYPE_HOTEL = 2;
    public static final int RESERVATION_TYPE_CAR = 3;
    public static final String INDEX_CUSTNAME = "custName";
    public static final String INDEX_RESERV_KEY = "resvKey";
    private String custName;

    private int resvType;

    private String resvKey;

    private int price;

    private boolean isdeleted = false;

    public Reservation(String custName, int resvType, String resvKey, int price) {
        this.custName = custName;
        this.resvType = resvType;
        this.resvKey = resvKey;
        this.price = price;
    }

    public Object getIndex(String indexName) throws InvalidIndexException {
        if (indexName.equals(INDEX_CUSTNAME))
            return custName;
        else if (indexName.equals(INDEX_RESERV_KEY))
            return resvKey;
        else
            throw new InvalidIndexException(indexName);
    }

    public Object getKey() {
        return new ReservationKey(custName, resvType, resvKey);
    }

    /**
     * @return Returns the custName.
     */
    public String getCustName() {
        return custName;
    }

    /**
     * @return Returns the resvKey.
     */
    public String getResvKey() {
        return resvKey;
    }

    /**
     * @return Returns the resvType.
     */
    public int getResvType() {
        return resvType;
    }

    public Object clone() {
        Reservation o = new Reservation(getCustName(), getResvType(),
                getResvKey(), getPrice());
        o.isdeleted = isdeleted;
        return o;
    }

    public int getPrice() {
        return price;
    }
}