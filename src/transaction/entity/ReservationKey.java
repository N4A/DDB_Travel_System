/*
 * Created on 2005-5-17
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package transaction.entity;

import java.io.Serializable;

/**
 * @author RAdmin
 * <p>
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class ReservationKey implements Serializable {
    protected String custName;

    protected int resvType;

    protected String resvKey;

    public ReservationKey(String custName, int resvType, String resvKey) {
        this.custName = custName;
        this.resvKey = resvKey;
        this.resvType = resvType;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof ReservationKey))
            return false;
        if (this == o)
            return true;
        ReservationKey k = (ReservationKey) o;
        if (k.custName.equals(custName) && k.resvKey.equals(resvKey) && k.resvType == resvType)
            return true;
        return false;
    }

    public int hashCode() {
        return custName.hashCode() + resvType + resvKey.hashCode();
    }

    public String toString() {
        StringBuffer buf = new StringBuffer("[");
        buf.append("customer name=");
        buf.append(custName);
        buf.append(";");
        buf.append("resvKey=");
        buf.append(resvKey);
        buf.append(";");
        buf.append("resvType=");
        buf.append(resvType);
        buf.append("]");

        return buf.toString();
    }
}