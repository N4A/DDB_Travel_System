package lockmgr;

import java.util.*;

class LockManagerTest {

    static LockManager lm = new LockManager();

    public static void main(String[] args) {
        test1();
        test2();
    }

    static void test1() {
        System.out.println("Deadlock test");

        Transaction t1 = new Transaction(1, "rl a sl 3000 wl b ua");
        Transaction t2 = new Transaction(2, "rl b sl 10 wl a ua");
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
        }
    }

    static void test2() {
        System.out.println("Lock conversion test");
        Transaction t1 = new Transaction(1, "rl a sl 10 wl a ua");
        Transaction t2 = new Transaction(2, "rl a sl 2000 ua");
        t1.start();
        t2.start();
        try {
            t1.join();
            t2.join();
        } catch (Exception e) {
        }
    }

    static class Transaction extends Thread {

        int xid;

        StringTokenizer st;

        public Transaction(int xid, String ops) {
            this.xid = xid;
            st = new StringTokenizer(ops);
        }

        public void run() {
            try {
                while (st.hasMoreTokens()) {
                    String opcode = st.nextToken();

                    if (opcode.equalsIgnoreCase("rl")) {
                        String param = st.nextToken();
                        lm.lock(xid, param, LockManager.READ);
                        System.out.println("Transaction " + xid + " got rl(" + param + ")");
                    } else if (opcode.equalsIgnoreCase("wl")) {
                        String param = st.nextToken();
                        lm.lock(xid, param, LockManager.WRITE);
                        System.out.println("Transaction " + xid + " got wl(" + param + ")");
                    } else if (opcode.equalsIgnoreCase("ua")) {
                        lm.unlockAll(xid);
                    } else if (opcode.equalsIgnoreCase("sl")) {
                        String param = st.nextToken();
                        int sleepTime = Integer.parseInt(param);
                        try {
                            this.sleep(sleepTime);
                        } catch (InterruptedException ie) {
                        }
                    } else {
                        System.out.println("Unknown opcode " + opcode);
                        break;
                    }
                }
            } catch (DeadlockException de) {
                System.out.println("Transaction " + xid + ": Deadlock...");
            } finally {
                lm.unlockAll(xid);
            }
        }
    }
}