package lockmgr;

import java.util.Date;

public class TimeObj extends XObj {
    private Date date = new Date();

    // The data members inherited are
    // XObj:: private int xid;

    TimeObj() {
        super();
    }

    TimeObj(int xid) {
        super(xid);
    }

    public long getTime() {
        return date.getTime();
    }
}