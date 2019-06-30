package lockmgr;

public class XObj {
    protected int xid = 0;

    XObj() {
        super();
        this.xid = 0;
    }

    XObj(int xid) {
        super();

        if (xid > 0) {
            this.xid = xid;
        } else {
            this.xid = 0;
        }
    }

    public String toString() {
        String outString = new String(this.getClass() + "::xid(" + this.xid + ")");
        return outString;
    }

    public int getXId() {
        return this.xid;
    }

    public int hashCode() {
        return this.xid;
    }

    public boolean equals(Object xobj) {
        if (xobj == null)
            return false;

        if (xobj instanceof XObj) {
            if (this.xid == ((XObj) xobj).getXId()) {
                return true;
            }
        }
        return false;
    }

    public Object clone() {
        try {
            XObj xobj = (XObj) super.clone();
            xobj.SetXId(this.xid);
            return xobj;
        } catch (CloneNotSupportedException clonenotsupported) {
            return null;
        }
    }

    public int key() {
        return this.xid;
    }

    // Used by clone.
    public void SetXId(int xid) {
        if (xid > 0) {
            this.xid = xid;
        }
        return;
    }
}