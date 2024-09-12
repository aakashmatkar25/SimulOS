package vit.os.OperatingSystem;
import java.io.*;
import java.util.*;
class PCB{
    public int jobID ;
    public int TTL ;
    public int TLL ;
    public int TTC ;
    public int TLC ;
    PCB(int jobID, int TTL, int TLL, int TTC, int TLC){
      this.jobID = jobID ;
      this.TTL = TTL ;
      this.TLL = TLL ;
      this.TTC = TTC ;
      this.TLC = TLC ;
    }
}
class MyOS{
    public char[][] M;
    public char[] IR;
    public char[] R;
    public int IC ;
    public int C ;
    public int SI ;
    public int PI ;
    public int TI ;
    public int EM ;
    public int RA ;
    public boolean nullPTR  ;
    public ArrayList<Character> allDigits ;
    public ArrayList<Integer> pageTableEntries ;
    public char[] buffer;
    public int memory_used ;
    public char[] PTR ;
    public boolean flagRead ;
    public boolean flagStore ;
    public boolean flagWrite ;
    public PCB pcb ;
    String opcode ;
    String operand ;
    LinkedList<String> fr ;
    FileWriter fw ;
    public MyOS(LinkedList<String> fr) {
        this.fr = fr ;
    }
    public int charArrayToInt(char[] arr){
        int res = 0 ;
        for(int i=0;i<4;i++){
            res = res * 10 + Character.getNumericValue(arr[i]);
        }
        return res ;
    }
    public char[] intToCharArray(int a){
        char[] res = new char[4] ;
        char c = '1';
        int g = 3 ;
        while(a>0){
            c =     (char) (a%10 + '0');
            res[g--] = c ;
            a = a / 10 ;
        }
        for(int i=0;i<4;i++){
            if((int)(res[i])==0){
                res[i]='0';
            }
        }
        return  res ;
    }
    public int randomInt(){
        Random rand = new Random();
        return rand.nextInt(30);
    }
    public void printMemory(){
        for(int i=0;i<300;i++){
            System.out.print("Memory["+i+"] : ");
            for(int j=0;j<4;j++){
                System.out.print(M[i][j]+" ");
            }
            System.out.println();
        }
    }
    public void INIT(){
        // initialization of all variables
        M = new char[300][4]; IR = new char[4]; R = new char[4] ; PTR = new char[4] ;
        IC = 0 ; C = 0; TI = 0 ; PI = 0 ; SI = 0 ; RA = 0 ; memory_used = 0 ; EM = 0 ;
        flagRead = false ; flagStore = false ; flagWrite = true ; nullPTR = true; opcode = "" ; operand = "" ;
        pageTableEntries = new ArrayList<>();
        allDigits = new ArrayList<>();
        allDigits.add('0');allDigits.add('1');allDigits.add('2');allDigits.add('3');allDigits.add('4');
        allDigits.add('5');allDigits.add('6');allDigits.add('7');allDigits.add('8');allDigits.add('9');

        // Initialization of PCB
        char[] tempArr = new char[4] ;
        for(int i=0;i<4;i++){
            tempArr[i] = buffer[i+4];
        }
        int jobID = charArrayToInt(tempArr);
        for(int i=0;i<4;i++){
            tempArr[i] = buffer[i+8];
        }
        int TTL = charArrayToInt(tempArr);
        for(int i=0;i<4;i++){
            tempArr[i] = buffer[i+12];
        }
        int TLL = charArrayToInt(tempArr);
        pcb = new PCB(jobID,TTL,TLL,0,0);
        for(int i=0;i<300;i++){
            for(int j=0;j<4;j++){
                M[i][j] = '-' ;
            }
        }

    }
    public int ALLOCATE(){
        if(nullPTR){
            System.out.println("Control Card Detected.................");
            int a = randomInt();
            System.out.println("Generated is : "+a);
            pageTableEntries.add(a);
            PTR = intToCharArray(a*10);
            System.out.print("Contents of PTR are ==> ");
            for (char t:PTR){
                System.out.print(t+" ");
            }
            System.out.println();
            System.out.println();
            int pageTableAddress = charArrayToInt(PTR);
            for(int i=pageTableAddress;i<pageTableAddress+10;i++){
                for(int j=0;j<4;j++){
                    M[i][j] = '?' ;
                }
            }
//            printMemory();
            nullPTR = false ;
            return pageTableAddress ;
        }else{
           int r = randomInt();
           while(pageTableEntries.contains(r)){
               r = randomInt();
           }
           pageTableEntries.add(r);
            System.out.println("Page Table Entry Genetated ==> "+r);
            System.out.println();
           char[] tempArr;
           tempArr = intToCharArray(r);
           int pageTableAddress = charArrayToInt(PTR);
           int tmp = 0 ;
           while(tmp<10){
               if(M[pageTableAddress][0]=='?'){
                   for(int i=0;i<4;i++){
                       M[pageTableAddress][i] = tempArr[i];
                   }
                   return charArrayToInt(tempArr)*10 ;
               }else{
                   pageTableAddress++;
                   tmp++;
               }
           }
               System.out.println("!!!!!!!!!!!!!!!!!! Page Table is FUll !!!!!!!!!!!!!!!!!!!!!!!");
               return -1;
        }
    }
    public void AddressMap(int VA) {
        if (VA >= 0 && VA < 100) {
            int PTE = charArrayToInt(PTR) + VA / 10;
//            System.out.println("PTE in AddressMap is : "+PTE);
            if(M[PTE][0]=='?'){
                System.out.println("Page Fault Occured");
                if(opcode.equals("GD")){
                    System.out.println("Valid Page Fault ");
                    ALLOCATE();
                    IC-- ;
                    flagRead = true ;
                    return;
                }else if(opcode.equals("SR")){
                    System.out.println("Valid Page Fault ");
                    ALLOCATE();
                    IC-- ;
                    flagStore = true ;
                    return;
                } else{
                    flagWrite = false;
                    PI = 3 ;
                    return;
                }
            }
            char[] tempArr = new char[4];
            for (int i = 0; i < 4; i++) {
                tempArr[i] = M[PTE][i];
            }
            RA = charArrayToInt(tempArr) * 10 + VA % 10;
//            System.out.println("RA is : " + RA);
        }else{
            System.out.println("Operand Error");
            PI = 2 ;
            return ;
        }
    }
    public void READ() throws Exception{
        if(pcb.TTC < pcb.TTL-1) {
            String line = "";
            int op = Integer.parseInt(operand);
            System.out.println("Operand given is " + op);
            AddressMap(op);
            if (flagRead) {
                flagRead = false;
                return;
            }
            if(fr.element().startsWith("$END")){
                System.out.println("EM : 1 ==> Out of Data Error ...........");
//                ABORT();
                TERMINATE();
                return ;
            }
            line = fr.remove();
//        buffer = line.toCharArray();
            char[] temp = line.toCharArray();
            System.out.println("Memory location in READ is : " + RA);
            int index = 0;
            for (int i = RA; i < RA + 10; i++) {
                for (int j = 0; j < 4; j++) {
                    if (index < temp.length) {
//                    System.out.println("Total time limit is : "+pcb.TTL+" current time counter is : "+pcb.TTC);
//                    if(pcb.TTC < pcb.TTL-1){
                        M[i][j] = temp[index++];
//                    }else{
//                        EM = 3 ;
//                        FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt",true);
//                        fw.write("EM = 3 : Total Time Limit Exceeded..................");
//                        fw.close();
//                        i = RA + 10 ;
//                        j = 4 ;
//                        TERMINATE();
//                    }
                    } else {
                        break;
                    }
                }
            }
            pcb.TTC += 2;
            System.out.println("------ After Executing READ function ------");
            System.out.println("TTC after GD ===> "+pcb.TTC);
            printMemory();
        }else{
            EM = 3 ;
            FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt",true);
            fw.write("EM = 3 : Total Time Limit Exceeded while READ..................");
            fw.close();
            TERMINATE();
        }
    }
    public void TERMINATE() throws Exception{
//        if(pcb.TTC < pcb.TTL){
//        IC = memory_used / 4 + 1 ;
//        while(!fr.isEmpty()) {
//            fr.remove();
//        }
//        pcb.TTC++ ;
//        }else{
//            EM = 3 ;
//            FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt",true);
//            fw.write("EM = 3 : Total Time Limit Exceeded while TERMINATE..................");
//            fw.close();
//        }
        IC = memory_used / 4 + 1 ;
        while(!fr.element().startsWith("$END")) {
            fr.remove();
        }
        fr.remove();
//        System.out.println("============!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!==============");
//        System.out.println("........... Program Terminated Successfully! ..............");
//        System.out.println("*************!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!**************");
        FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt", true);
        fw.write("Job Id : "+pcb.jobID+"\t"+" TTL : "+pcb.TTL+" TLL : "+pcb.TLL+" TTC : "+pcb.TTC+" TLC : "+pcb.TTC);
        fw.write("SI : "+SI+"\t"+" PI : "+PI+"\t"+" TI : "+TI);
        if(PI==1){
            fw.write("Opcode Error Terminated The program.........");
        }
        if(PI==2){
            fw.write("Operand Error Terminated the program......");
        }
        if(PI==3){
            fw.write("INVALID PAGE FAULT......");
        }
        if(EM==1){
            fw.write("Out of Data Error Terminated the program........");
        }
        fw.write("\nOne Program Terminated");
        fw.write("\n");
        fw.write("\n");

        fw.close();
    }
    public void ABORT() throws Exception{
        IC = memory_used / 4 + 1 ;
        while(!fr.isEmpty()) {
            fr.remove();
        }
        System.out.println("============!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!==============");
        System.out.println("........... Program Terminated due to Error ...............");
        System.out.println("*************!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!**************");
    }
    public void WRITE() throws Exception {
    if(pcb.TTC < pcb.TTL) {
        System.out.println("Entered into the write function");
        int oper = Integer.parseInt(operand);
        AddressMap(oper);
        if (flagWrite) {
        FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt", true);
        for (int i = RA; i < RA + 10; i++) {
            for (int j = 0; j < 4; j++) {
                if (M[i][j] != '-') {
                    if (pcb.TLC < pcb.TLL) {
                        fw.write(M[i][j]);
                    } else {
                        EM = 2;
                        fw.write("EM = 2 : Total Line Limit Exceeded while WRITE...........................");
//                        ABORT();
                        TERMINATE();
                        i = RA+10 ; j = 4 ;
//                        return;
                    }
                }
            }
        }
        pcb.TLC++;
        fw.write("\n");
        pcb.TTC++;
        fw.close();
        System.out.println("TTC after PD ===> " + pcb.TTC);
    }else{
            return;
        }
        } else {
            EM = 3;
            TI = 2 ;
            FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt", true);
            fw.write("EM = 3 : Total Time Limit Exceeded while WRITE..................");
            fw.close();
//            ABORT();
        TERMINATE();
        }
    }
    public void MOS() throws Exception{
        System.out.println("Entered into MOS");
       if(TI==0 && SI==1) {
           System.out.println("Entered into READ");
           READ();
       }
       if(TI==0 && SI==2) {
           System.out.println("Entered into WRITE");
           WRITE();
       }
       if(TI==0 && SI==3) {
           System.out.println("Entered into TERMINATE");
           TERMINATE();
       }
        if(TI==2 && SI==1) {
            TERMINATE();
//            ABORT();
        }
        if(TI==2 && SI==2) {
            FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt",true);
            fw.write("EM = 3 : Total Time Limit Exceeded ..................");
            fw.close();
            TERMINATE();
//            ABORT();
        }
        if(TI==2 && SI==3) {
            TERMINATE();
//            ABORT();
        }
        if(TI==0 && PI==1){
            System.out.println("Opcode Error Terminated The Program................");
            TERMINATE();
//            ABORT();
        }
        if(TI==0 && PI==2){
            System.out.println("Operand Error Terminated the Program................");
            TERMINATE();
//            ABORT();
        }
       if(TI==0 && PI==3) {
           System.out.println("Entered into MOS for InValid page fault");
           flagWrite = false ;
           TERMINATE();
//           ABORT();
//           TERMINATE();
//           if(opcode.equals("GD") || opcode.equals("SR")){
//               System.out.println("Valid Page Fault ");
//               RA = ALLOCATE();
//               System.out.println("Memory address after handling page fault is : "+RA);
////               printMemory();
//               IC-- ;
//               System.out.println("IC is decremented to  "+IC);
//               PI = 0 ;
//           }
//           else{
//               System.out.println("Invalid Page fault occured");
//           }
       }
       if(TI==2 && PI==1){
           TERMINATE();
//           ABORT();
       } if(TI==2 && PI==2){
           TERMINATE();
//           ABORT();
       } if(TI==2 && PI==3){
           TERMINATE();
//           ABORT();
       }
    }
    public void STARTEXECUTION() throws Exception{
        System.out.println("Data Card Detected");
        System.out.println("Memory Used is : "+memory_used);
//        IC = 0 ;
//        AddressMap(IC);
        int iterations = 0 ;
        IC = 0 ;
       while(IC<=memory_used/4){
           AddressMap(IC);
           System.out.println("Iteration ==> "+iterations+" Current IC value ==> "+IC);
           for(int i=0;i<4;i++){
               IR[i] = M[RA][i];
           }
           IC++ ;
//           System.out.println("IC incremented to ==> "+IC);
//           System.out.println();
           System.out.println("Contents of IR is : ");
           for(int j=0;j<4;j++){
               System.out.print(IR[j]+" ");
           }
           System.out.println();

           if(IR[0]=='H'){
               opcode = "H" ;
           }else{
               opcode = IR[0] + "" +IR[1] ;
           }
           if(allDigits.contains(IR[2]) && allDigits.contains(IR[3])){
               operand = IR[2] + "" + IR[3] ;
               System.out.println("Operand is set to "+operand);
           }else{
               if(opcode!="H"){
                   EM = 5;
                   PI = 2 ;
                   MOS();
                   break;
               }
           }
           System.out.println("opcode is : "+opcode);
           switch (opcode){
               case "GD":
                   SI = 1;
                   MOS();
                   break;
               case "PD":
                   SI = 2;
                   MOS();
                   break;
               case "LR":
                   LoadRegister();
                   break;
               case "CR":
                   CompareRegister();
                   break;
               case "BT":
                   JumpToLocation();
                   break;
               case "SR":
                   storeToLocation();
                   break;
               case "H":
                   SI = 3;
                   MOS();
                   break;
               default:
                   EM = 4;
                   PI = 1;
                   MOS();
                   break;
           }
           iterations++ ;
       }
    }
    public void storeToLocation() throws Exception{
        if(pcb.TTC < pcb.TTL - 1) {
            int memoryLocation = Integer.parseInt(operand);
            AddressMap(memoryLocation);
            if (flagStore) {
                flagStore = false;
                return;
            }
            for (int i = 0; i < 4; i++) {
                M[RA][i] = R[i];
            }
            pcb.TTC+=2;
        }else{
            EM = 3 ;
            FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt",true);
            fw.write("EM = 3 : Total Time Limit Exceeded while executing SR..................");
            fw.close();
            TERMINATE();
        }
    }
    public void JumpToLocation() throws Exception {
        if(pcb.TTC < pcb.TTL){
        if (C == 1) {
            int memoryLocation = Integer.parseInt(operand);
            IC = memoryLocation;
            System.out.println("IC is set to : "+IC);
        }
        pcb.TTC++;
        }else{
            EM = 3 ;
            FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt",true);
            fw.write("EM = 3 : Total Time Limit Exceeded while BT..................");
            fw.close();
        }
    }
    public void CompareRegister() throws Exception{
        if(pcb.TTC < pcb.TTL) {
            int memoryLocation = Integer.parseInt(operand);
            AddressMap(memoryLocation);
            String a = "";
            String b = "";
            for (int i = 0; i < 4; i++){
                a += R[i];
                b += M[RA][i];
            }
            System.out.println("string a is : " + a + " string b is : " + b);
            if (a.equals(b)) {
                C = 1;
            }
            pcb.TTC++;
        }else{
            EM = 3 ;
            FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt",true);
            fw.write("EM = 3 : Total Time Limit Exceeded while CR..................");
            fw.close();
            TERMINATE();
        }
    }
    public void LoadRegister() throws Exception {
        System.out.println("Inside LR : TTC ==> "+pcb.TTC+" TTL ==> "+pcb.TTL);
        if(pcb.TTC < pcb.TTL) {
            int memoryLocation = Integer.parseInt(operand);
            AddressMap(memoryLocation);
            for (int j = 0; j < 4; j++) {
                if (M[RA][j] != '-') {
                    R[j] = M[RA][j];
                } else {
                    break;
                }
            }
            System.out.println("Content in General purpose register are : ");
            for (int j = 0; j < 4; j++) {
                if ((int) M[memoryLocation][j] != 0) {
                    System.out.print(R[j] + " ");
                } else {
                    break;
                }
            }
            System.out.println();
            pcb.TTC++;
        }else{
            EM = 3 ;
            FileWriter fw = new FileWriter("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\output2.txt",true);
            fw.write("EM = 3 : Total Time Limit Exceeded while LR..................");
            fw.close();
            TERMINATE();
        }
    }
    public void load() throws Exception{
        String line = "";
        while(!fr.isEmpty()){
            line = fr.remove();
            buffer = line.toCharArray();
//            System.out.println("Current line is : "+line);
            if(buffer[0]=='$'&&buffer[1]=='A'&&buffer[2]=='M'&&buffer[3]=='J'){
                INIT();
                System.out.println("Page Table is at ==> "+ALLOCATE());
            }else if(buffer[0]=='$'&&buffer[1]=='D'&&buffer[2]=='T'&&buffer[3]=='A'){
                STARTEXECUTION();
            }else if(buffer[0]=='$'&&buffer[1]=='E'&&buffer[2]=='N'&&buffer[3]=='D'){
                System.out.println("END Card Detected......................");
                break;
            }else{
                System.out.println("Program Card Detected............");
                int memoryLocation = ALLOCATE() ;
                int a = 0;
            int HCount = 0 ;
            for(char c : buffer){
                if(c=='H') HCount++ ;
            }
           for(int i=memoryLocation;i<memoryLocation+10;i++){
               for(int j=0;j<4;j++){
                   if(a<buffer.length){
                       if(HCount>1 && buffer[a]=='H'){
                           M[i][j] = buffer[a++];
                           memory_used++ ;
                           HCount-- ;
                           i++;j--;
                           ;
                       }else{
                       M[i][j] = buffer[a++];
                       memory_used++ ;
                       }
                   }
               }
           }
//                for(int i=memoryLocation;i<memoryLocation+10;i++){
//                    for(int j=0;j<4;j++){
//                        if(a<buffer.length){
//                            M[i][j] = buffer[a++];
//                            memory_used++ ;
//                        }
//                    }
//                }
                printMemory();
            }
        }
    }
}
public class OperatingSystem_2 {
    public static void main(String[] args )throws Exception{
        File file = new File("C:\\Users\\HP\\OneDrive\\Desktop\\Java\\java_VIT\\vit\\os\\inp.txt");
        Scanner sc = new Scanner(file);
        LinkedList<String> ll = new LinkedList<>();
        while(sc.hasNextLine()){
            ll.add(sc.nextLine());
        }
        MyOS myOS = new MyOS(ll);
        myOS.load();
    }
}
