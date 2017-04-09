
import java.util.LinkedList;
import java.util.Scanner;

/**
 *
 * @author Tin
 */
public class FileSystem {

    static int[] FAT = new int[64];
    static long bitmap = 0L;
    static LinkedList<Inodes> nodeslist = new LinkedList();
    final static long pointer = 4611686018427387904L;

    public static void main(String[] args) {
        System.out.println("type 'exit' to quit");
        String input = "";
        Scanner kb = new Scanner(System.in);
        while (!input.equalsIgnoreCase("exit")) {
            System.out.print(">");
            input = kb.nextLine();
            String command[] = input.split(" ");
            if (command[0].equalsIgnoreCase("put")) {
                put(command[1]);
            } else if (command[0].equalsIgnoreCase("del")) {
                del(command[1]);
            } else if (command[0].equalsIgnoreCase("bitmap")) {
                printBitMap();
            } else if (command[0].equalsIgnoreCase("inodes")) {
                printINodes();

            } else if (!command[0].equalsIgnoreCase("exit")) {
                System.err.println("Command does not exist!");
            } 
        }
    }

    private static void put(String namesize) {
        String values[] = namesize.split(",");
        Inodes i_node = new Inodes();
        try {
            i_node.filename = values[0];
            i_node.size = Integer.parseInt(values[1]);
            if (isExist(i_node.filename) == true){
                System.err.println("File already exist");
                return;
            }
            if (isAvailable(i_node.size) == false) {
                System.err.println("Exceed memory capacity");
                return;
            }
            nodeslist.add(i_node);
            updateBitMap(i_node);

        } catch (NumberFormatException e) {
            System.err.println("Wrong input format");
            return;
        }
    }

    private static void del(String name) {
        int index = 0;
        if(isExist(name) == false){
            System.err.println("File does not exist");
            return;
        }
        for (Inodes node : nodeslist) {
            if (node.filename.equalsIgnoreCase(name)) {
                index = nodeslist.indexOf(node);
            }
        }
        
        Inodes delnode = nodeslist.remove(index);
        long delbit = 0L;
        int x = delnode.startBlock;
        while (x != -1) {
            if (x == 0) {
                delbit = delbit | (pointer << 1);
            } else {
                delbit = delbit | (pointer >> (x - 1));
            }
            x = FAT[x];
        }
        bitmap = bitmap ^ delbit;
    }
    
    private static boolean isExist(String name){
        boolean exist = false;
        for (Inodes node : nodeslist){
            if (node.filename.equalsIgnoreCase(name)){
                exist = true;
            }
        }
        return exist;        
    }
    private static void updateBitMap(Inodes node) {
        int[] arraybit = convertBitMap();
        for (int i = 0; i < arraybit.length; i++) {
            if (arraybit[i] == 0) {
                if (i == 0) {
                    bitmap = bitmap | (pointer << 1);
                    node.startBlock = 0;
                    break;
                } else {
                    bitmap = bitmap | (pointer >> (i - 1));
                    node.startBlock = i;
                    break;
                }
            }
        }
        int x = node.startBlock;
        int remainSize = node.size - 1;
        while (x != -1) {
            if ((x + 1) <= 63) {
                for (int i = x + 1; i < FAT.length; i++) {
                    if (arraybit[i] == 0) {
                        bitmap = bitmap | (pointer >> (i - 1));
                        remainSize -= 1;
                        FAT[x] = i;
                        x = i;
                        if (remainSize == 0) {
                            FAT[x] = -1;
                            x = -1;
                        }
                        break;
                    }
                }
            } else {
                for (int i = 0; i < FAT.length; i++) {
                    if (arraybit[i] == 0) {
                        if (i == 0) {
                            bitmap = bitmap | (pointer << 1);
                            remainSize -= 1;
                            FAT[x] = 0;
                            x = 0;
                            if (remainSize == 0) {
                                FAT[x] = -1;
                                x = -1;
                            }
                            break;
                        } else {
                            bitmap = bitmap | (pointer >> (i - 1));
                            remainSize -= 1;
                            FAT[x] = i;
                            x = i;
                            if (remainSize == 0) {
                                FAT[x] = -1;
                                x = -1;
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private static boolean isAvailable(int size) {
        boolean isEnough = false;
        int space = 0;
        int[] map = convertBitMap();
        for (int i = 0; i < map.length; i++) {
            if (map[i] == 0) {
                space++;
            }
        }
        if (space >= size) {
            isEnough = true;
        }
        return isEnough;
    }

    private static int[] convertBitMap() {
        final int bitCount = 64;
        int[] number = new int[bitCount];
        for (int i = 0; i < bitCount; i++) {
            number[i] = (int) ((bitmap >>> (bitCount - i - 1)) & 1);
        }
        return number;
    }

    private static void printINodes() {
        for (Inodes node : nodeslist) {
            int x = node.startBlock;
            System.out.print(node.startBlock);
            while (x != -1) {
                if (FAT[x] != -1) {
                    System.out.print(" -> " + FAT[x]);
                }
                x = FAT[x];
            }
            System.out.println();
        }
    }

    private static void printBitMap() {
        int[] number = convertBitMap();
        for (int i = 0; i < number.length; i++) {
            System.out.print(number[i]);
            if (((i + 1) % 8) == 0) {
                System.out.println();
            }
        }
    }

    static private class Inodes {

        String filename = null;
        int size = 0;
        int startBlock = -2;
    }
}
