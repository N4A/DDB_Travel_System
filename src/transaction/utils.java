package transaction;

import java.io.*;

/**
 * @author Duocai Wu
 * @Date 2019/7/23
 * @Time 10:35
 */
public class utils {

    public static boolean storeObject(Object o, String path) {
        File xidLog = new File(path);
        ObjectOutputStream oout = null;
        try {
            oout = new ObjectOutputStream(new FileOutputStream(xidLog));
            oout.writeObject(o);
            oout.flush();
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (oout != null)
                    oout.close();
            } catch (IOException e1) {
            }
        }
    }

    public static Object loadObject(String path) {
        File xidCounterLog = new File(path);
        ObjectInputStream oin = null;
        try {
            oin = new ObjectInputStream(new FileInputStream(xidCounterLog));
            return oin.readObject();
        } catch (Exception e) {
            return null;
        } finally {
            try {
                if (oin != null)
                    oin.close();
            } catch (IOException e1) {
            }
        }
    }
}
