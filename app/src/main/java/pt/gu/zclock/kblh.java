package pt.gu.zclock;

import java.util.ArrayList;
import java.util.List;

public class kblh {
	    
	public enum Ot {
		Alef(1),
        Bet(2),
        Gimel(3),
        Dalet(4),
        He(5),
        Vav(6),
        Zain(7),
        Chet(8),
        Tet(9),
        Iod(10),
        KafSofit(11),
        Kaf(12),
        Lamed(13),
        MemSofit(14),
        Mem(15),
        NunSofit(16),
        Nun(17),
        Samekh(18),
        Aiyn(19),
        PeSofit(20),
        Pe(21),
        TzadikSofit(22),
        Tzadik(23),
        Qof(24),
        Resh(25),
        Shin(26),
        Tav(27);
		
		private int index;
		Ot(int _index){
			this.index = _index;
		}
	}
	
	public static class AlefBet{
		
        public int Alef;
        public int Bet;
        public int Gimel;
        public int Dalet;
        public int He;
        public int Vav;
        public int Zain;
        public int Chet;
        public int Tet;
        public int Iod;
        public int KafSofit;
        public int Kaf;
        public int Lamed;
        public int MemSofit;
        public int Mem;
        public int NunSofit;
        public int Nun;
        public int Samekh;
        public int Aiyn;
        public int PeSofit;
        public int Pe;
        public int TzadikSofit;
        public int Tzadik;
        public int Qof;
        public int Resh;
        public int Shin;
        public int Tav;

        public AlefBet(Ot alef, Ot bet, Ot gimel, Ot dalet, Ot he, Ot vav, Ot zain, Ot chet, Ot tet,
            Ot iod, Ot finalkaf, Ot kaf, Ot lamed, Ot finalmem, Ot mem, Ot finalnun, Ot nun,
            Ot samekh, Ot ayin, Ot finalpe, Ot pe, Ot finaltzadik, Ot tzadik, Ot qof, Ot resh, Ot shin, Ot tav){
            
			Alef = alef.index;
            Bet = bet.index;
            Gimel = gimel.index;
            Dalet = dalet.index;
            He = he.index;
            Vav = vav.index;
            Zain = zain.index;
            Chet = chet.index;
            Tet = tet.index;
            Iod = iod.index;
            Lamed = lamed.index;
            KafSofit = finalkaf.index;
            Kaf = kaf.index;
            MemSofit = finalmem.index;
            Mem = mem.index;
            NunSofit = finalnun.index;
            Nun = nun.index;
            Samekh = samekh.index;
            Aiyn = ayin.index;
            PeSofit = finalpe.index;
            Pe = pe.index;
            TzadikSofit = finaltzadik.index;
            Tzadik = tzadik.index;
            Qof = qof.index;
            Resh = resh.index;
            Shin = shin.index;
            Tav = tav.index;
        }
		
		public AlefBet(){};

        public int[] ToArray(){
			
            return new int[]{
                    Alef,Bet,Gimel,Dalet,He,Vav,Zain,Chet,Tet,
                    Iod,KafSofit,Kaf,Lamed,MemSofit,Mem,NunSofit,Nun,
                    Samekh,Aiyn,PeSofit,Pe,TzadikSofit,Tzadik,Qof,Resh,Shin,Tav};
        }

        public void NoSofit()
        {
            Tzadik += TzadikSofit;
            TzadikSofit = 0;
            Pe += PeSofit;
            PeSofit = 0;
            Nun += NunSofit;
            NunSofit = 0;
            Mem += MemSofit;
            MemSofit = 0;
            Kaf += KafSofit;
            KafSofit = 0;
        }

        public void FromArray(int[] array)
        {
            if (array.length < 27) return;
            Alef=array[0];
            Bet=array[1];
            Gimel = array[2];
            Dalet = array[3];
            He = array[4];
            Vav = array[5];
            Zain = array[6];
            Chet = array[7];
            Tet = array[8];
            Iod = array[9];
            KafSofit = array[10];
            Kaf = array[11];
            Lamed = array[12];
            MemSofit = array[13];
            Mem = array[14];
            NunSofit = array[15];
            Nun = array[16];
            Samekh = array[17];
            Aiyn = array[18];
            PeSofit = array[19];
            Pe = array[20];
            TzadikSofit = array[21];
            Tzadik = array[22];
            Qof = array[23];
            Resh = array[24];
            Shin = array[25];
            Tav = array[26];
        }
        public AlefBet MisparOtiot(String text){
            int[] ab = new int[27];
            for (char c : text.toCharArray())
            {
                int i = c - 0x05D0;
                if (i > -1 && i < 27) ab[i]++;
            }
            AlefBet a = new AlefBet();
            a.FromArray(ab);
            return a;
        }
	}
	
    public enum Chilufi {
	    Albam       (new int[] {13,15,17,18,19,21,23,24,25,26,27,27,1,2,2,3,3,4,5,6,6,7,7,8,9,10,12}),
	    Atbash      (new int[] {27,26,25,24,23,21,19,18,17,15,13,13,12,10,10,9,9,8,7,6,6,5,5,4,3,2,1}),
	    Achbi       (new int[] {12,10,9,8,7,6,5,4,3,2,1,1,27,26,26,25,25,24,23,20,21,19,19,18,17,15,13}),
	    AyiqBekher  (new int[] {10,12,13,15,17,18,19,21,23,24,5,25,26,6,27,7,11,14,16,8,20,9,22,1,2,3,4}),
	    AchasBeta   (new int[] {8,9,10,12,13,15,17,18,19,21,23,23,24,25,25,26,26,1,2,3,3,4,4,5,6,7,27}),
	    Atbach      (new int[] {9,8,7,6,5,4,3,2,1,23,20,21,19,18,18,16,17,15,13,11,12,10,10,27,26,25,24});

        private int[] array;
        Chilufi(int[] _array){
            this.array=_array;
        }

        public int[] getArray() {
            return this.array;
        }
    }

	public enum Mispar {
        MisparHechrachi (new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),  //0-Standard
        MisparGadol     (new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 500, 20, 30, 600, 40, 700, 50, 60, 70, 800, 80, 900, 90, 100, 200, 300, 400}),      //1-Sofit
        Milui72         (new int[] { 111, 412, 83, 434, 10, 12, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 }),          //2
        Milui63         (new int[] { 111, 412, 83, 434, 15, 13, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 }),          //3
        Milui45         (new int[] { 111, 412, 83, 434, 6, 13, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 }),          //4
        Milui52         (new int[] { 111, 412, 83, 434, 10, 12, 67, 418, 419, 20, 100, 100, 74, 80, 80, 106, 106, 120, 130, 81, 81, 104, 104, 186, 510, 360, 406 }),          //5
        MisparSiduri    (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),     //6-Contagem ordinal 1>22
        MisparQatan     (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),      //7-sem zeros iod=1, kaf=2,...
        MisparQidmi     (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),      //8-Standard triangular 1,3,6...(n^2+n)/2
        MisparPerati    (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),     //9-Standard quadratico 1,4,9...n^2
        MisparNeelam    (new int[]{ 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 20, 20, 30, 40, 40, 50, 50, 60, 70, 80, 80, 90, 90, 100, 200, 300, 400 }),      //10
        MisparTagin     (new int[]{ 0, 1, 3, 1, 1, 0, 3, 1, 3, 1, 0, 0, 0, 0, 0, 3, 3, 0, 3, 0, 0, 3, 3, 1, 0, 3, 0 });      //10
	    //QatanMispari(4),

        private int[] array;
        Mispar(int[] _array){
            this.array=_array;
        }

        public int[] getArray() {
            return this.array;
        }
    }

	public enum Ntrikon {
	    start,
	    middle,
	    end;
	}

    public static class hString{

        public enum hFormat{
            Taamim,
            Nequdot,
            Otiot,
            OtiotSequence;
        }

        private String text;
        public hString(String text){
            this.text=text;
        }

        public String getText(){
            return this.text;
        }

        public String format(hFormat format){
            return format(format,this.text);
        }

        private String format(hFormat format, String s){
            if (format.equals(hFormat.Nequdot)){
                return getNekudot(s);
            } else if (format.equals(hFormat.Otiot)){
                return getOtiot(s);
            } else if (format.equals(hFormat.OtiotSequence)){
                return getOtiotSeq(s);
            }
            return getTaamim(s);
        }

        public String format(hFormat format,boolean removeMaqaf, boolean removePBreaks){
            String s = text;
            if (removeMaqaf) s = removeMaqaf(s);
            if (removePBreaks) s = removeBreakSymbs(s);
            return format(format, s);
        }

        public void removeMaqaf(){
            this.text = removeMaqaf(this.text);
        }
        private String removeMaqaf(String s){
            return s.replace("\u05be"," ");
        }
        public void removeBreakSymbs(){
            this.text = removeBreakSymbs(this.text);
        }
        private String removeBreakSymbs(String s){
            return s.replace("{\u05e1}", "\u05c8")
                    .replace("{\u05e4}","\u05c9")
                    .replace("{\u05e8}","\u05ca")
                    .replace("{\u05e9}","\u05cb");
        }

        private String getNekudot(String s){
            String res = "";
            for (char c : s.toCharArray()) {
                if (c < 0x041 || c == 0x05be || (c > 0x05af && c < 0x05eb && c != 0x05bd && c!=0x05c0)) {
                    res += c;
                }
            }
            return res;
        }

        private String getOtiot(String s){
            String res = "";
            for (char c : s.toCharArray()) {
                if ((c > 0x05cf && c < 0x05eb) || c==0x020) res += c;
            }
            return res;
        }

        private String getOtiotSeq(String s){
            String res = "";
            for (char c : s.toCharArray()) {
                if (c > 0x05cf && c < 0x05eb) res += c;
            }
            return res;
        }

        private String getTaamim(String s){
            String res = "";
            for (char c : s.toCharArray()) {
                if ((c > 0x0590 && c < 0x05f5) || c==0x020) res += c;
            }
            return res;
        }

        public String[] getMilim(){
            return text.split(" ");
        }

        public String[] getMilim(hFormat format){
            return format(format,true,true).split(" ");
        }
    }

	public static class Gematria {
        private String _txt;
        public String Text;

        private String[] milim;
        private String otiotseq;

        private hString hebString;

        private int _val;
        public int Value;

        public Gematria(String text) {
            Text = text;
            hebString = new hString(Text);
            milim = hebString.format(hString.hFormat.Otiot,true,true).split("\\s+");
            otiotseq = hebString.getOtiotSeq(Text);
        }

        public Gematria(hString text){
            hebString = text;
            Text = text.text;
            milim = text.format(hString.hFormat.Otiot,true,true).split("\\s+");
            otiotseq = text.getOtiotSeq(Text);
        }

        public String[] getMilim(){
            return milim;
        }

        public String getOtiotseq(){
            return otiotseq;
        }

        //region Gematria
        public int getGematria(Mispar mispar){
            return getGematria(mispar.getArray(),Text);
        }


        public int[] getGematriaArray(Mispar mispar){
            int[] t = mispar.getArray();
            int[] res = new int[milim.length];
            for (int i=0;i<milim.length;i++){
                res[i] = getGematria(t,milim[i]);
            }
            return res;
        }

        private int getGematria(int[] table,String txt) {
            int g = 0;
            for (char c : txt.toCharArray()) {
                int i = c - 0x05d0;
                if (i >= 0 && i < 27) g += table[i];
            }
            return g;
        }

        //endregion

        //region chilufi otiot

        private AlefBet _chilufitable;
        public AlefBet ChilufiTable;

        public String ChilufiOtiot(Chilufi ChilufiMethod) {
            return GetChilufi(ChilufiMethod.getArray(),Text);
        }

        public String ChilufiOtiot(AlefBet table) {
            return GetChilufi(table.ToArray(), Text);
        }

        public String getChilufi(Chilufi chilufi){
            return GetChilufi(chilufi.getArray(),hebString.format(hString.hFormat.Otiot));
        }

        public String getChilufi(Chilufi chilufi, hString.hFormat format){
            hString hs = new hString(Text);
            String s = hs.format(format);
            return GetChilufi(chilufi.getArray(),s);
        }

        private String GetChilufi(int[] tbl,String s) {
            String res = "";
            for(char c : s.toCharArray())
            {
                int i = c - 0x05d0;
                res += (i >= 0 && i < 27) ? (char) (tbl[i] + 0x05cf) : c;
            }
            return res;
        }

        public String[][] getMatrix(int rows, int columns){
            if (rows*columns!=otiotseq.length()) return null;
            String[][] result = new String[rows][columns];
            char[] chars = otiotseq.toCharArray();
            for (int r = 0;r<rows;r++) {
                for (int c = 0; c < columns; c++) {
                    result[r][c] = String.valueOf(chars[r * columns + c]);
                }
            }
            return result;
        }

        private int[] factorize(int number){

            List<Integer> factors = new ArrayList<>();
            int b = 1;
            while (b <= number)
            {
                if (number % b == 0)
                {
                    factors.add(b);
                    number = number / b;
                    b = 1;
                }
                b++;
            }
            int[] res = new int[factors.size()];
            int i=0;
            for (Integer integer : factors){
                res[i++] = integer;
            }
            return res;
        }
    }
    //endregion
}