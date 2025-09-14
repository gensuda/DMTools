package structures;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


// java port based on SavedGameDecoder Written by Christophe Fontanel
//DM savegame characters transfer from PC savegame to Amiga savegame
//copyright 2025 Ján Mojžiš, janmojzisx@gmail.com, github https://github.com/gensuda/DMTools

public class DMPC {
	public class ChampionData{
		  // ************* NOTE WELL !! *************
		  // 3328 bytes read into this part of the structure.
		  // This includes the four characters and 128 additional
		  // bytes of global data.
		  public Champion[] m_Characters = new Champion[4];
		  short brightness;
		  byte  SeeThruWalls;
		  byte  Byte13279;
		  short Word13278;       //Party shield
		  short Word13276;       //Spell effect
		  short Word13274;       //Spell effect
		  byte  NumHistEnt;      // #entries in 13268???
		  byte uByte13271;      //Life frozen time
		  byte  Byte13270;       //index of entry in 13268
		  byte  Byte13269;       //index of entry in 13268
		  short[] Word13268 = new short[24];   //history of moves???
		                       // bits 0-4 = mapX
		                       // bits 5_9 = mapY
		                       // bits 10_15 = loaded level
		  byte[]  Byte13220 = new byte[24];   //parallels 13268??
		                       // Is this a once-only kind of thing?
		  byte  Invisible;       // 13196;
		  byte[]  Byte13195 = new byte[41];
		  /////////////  End of Character portion of save file /////////////
		  //  ***************************************************
		
	}
	public class Champion{
		public  byte[] name = new byte[8];  // 00
		public  byte[] title = new byte[16];// 08 // size??
		  short wordx24; // Not swapped because I don't see it used.
		  byte[] FILL26 = new byte[28-26];
		  byte facing;  //28
		  byte position; //29
		  public  byte byte30;
		  byte byte31;
		  byte attackType; //32Signed so check for -1 works.
		  byte byte33;
		  byte[] incantation = new byte[4];//34;
		        // [0] = power 96 through 111 --> 1 through 6
		        // [1] =      102 through 107
		        // [2] =      108 through 113
		        // [3] =      114 through 119
		  byte[] FILL38 = new byte[40-38];
		  byte facing3; //40
		  byte uByte41;
		  byte uByte42; //Poison count.  A timer for each one??
		  byte uByte43;
		  short busyTimer;// 44 // Timer Index. Signed so -1 works.
		  short timerIndex;//46;
		  short word48; // ORed with 0x280
		              // 0x8000 while selecting attack option
		  short ouches;     //50;// mask of damaged body parts.
		  public short HP;         //52
		  public short maxHP;      //54
		  public short stamina;    //56;
		  public short maxStamina; //58;
		  public short mana;       //60;
		  public short maxMana;    //62;
		  short word64;
		  public short food;       //66;
		  public short water;      //68;
		  public Attribute[] Attributes = new Attribute[7]; // 70 (maximum, current, minimum)
		                           // 70 = [0] Luck??
		                           // 73 = [1] Strength
		                           // 76 = [2] Dexterity
		                           // 79 = [3] Wisdom
		                           // 82 = [4] Vitality
		                           // 85 = [5] AntiMagic
		                           // 88 = [6] AntiFire
		//  byte  FILL91;
		  public  Skill[] skills92 = new Skill[4]; // 92 //0 and 4-7  =Fighter
		                            //1 and 8-11 =Ninja
		                            //2 and 12-15=Priest
		                            //3 and 16-19=Wizard
		 // byte[]  possessions = new Rn[30]; //212
		  short[] possessions = new short[30];

		  short load;//272; In 10ths of KG
		  short shieldStrength; //274;
		  //byte[] FILL276 = new byte[336-276];
			 // byte[] unknown = new byte[44+111+5];
		  short[] unknown = new short[(44+111+5)/2];
		  public BufferedImage portrait;
		  
		  //byte[] portrait = new byte[/*8*16 - 11*/1]; //336
		  //byte[] FILL337 = new byte[800-337];		
	}
	public class Skill{ // 6-byte structure in character
	
		public short word0;
		public int   Long2;  // Experience in this skill
	};	
	public class Attribute{ // 3-byte structure in character
	  // Let me tell you why I hid these variables.....
	  // From what I can tell, these are treated as unsigned
	  // bytes.  But at least one of them can go negative.
	  // The Luck-Minimum starts at 10 and is decreased by
	  // three for each cursed object.  Gather four such
	  // objects and you have set the minimum to a very
	  // large number.  So I want to handle them exactly the
	  // same except that when you ask for a number and it
	  // has gone negative, I will return a zero.  But its
	  // value will export to the Atari perfectly.
		public byte ubMaximum;
		public byte ubCurrent;
		public byte ubMinimum;
	}	
	
	/*
	 *   NEOPHYTE                       asi 0 -  1999
	 *   NOVICE     1250 - 1750         asi 1000-1999
	 *   APPRENTICE 2750 - 3000 - 3500  asi 2000-3999
	 *   JOURNEYMAN 4000                asi 
	 */
	public ChampionData championdata = new ChampionData();
	public DMPC() {
		// TODO Auto-generated method stub
		/*
		load("d:\\hry\\DM\\DMSAVE.DAT-06-ChampionData.bin");
		display();*/
		allocateChampions();
	}
	/*
	public void display() {
		for (int i = 0; i < 4; i++) {
			vypis(championdata.m_Characters[i]);
			System.out.println("-----------------------------------------");
		}
		
	}*/
	private void allocateChampions() {
		byte[] reds   = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues  = new byte[256];
		
		for (int i = 0; i < Palettes.PCDMpaletkaRGB.length; i++) {
			reds[i] = (byte)Palettes.PCDMpaletkaRGB[i][0];
			greens[i] = (byte)Palettes.PCDMpaletkaRGB[i][1];
			blues[i] = (byte)Palettes.PCDMpaletkaRGB[i][2];
		}
		
		for (int i = 0; i < 4; i++) {
			championdata.m_Characters[i] = new Champion();
			IndexColorModel icm = new IndexColorModel(8,256,reds, greens, blues);
			championdata.m_Characters[i].portrait = new BufferedImage(32,29,BufferedImage.TYPE_BYTE_INDEXED, icm);
		}
			
		
		for (int a = 0 ; a < 7; a++) {
			championdata.m_Characters[0].Attributes[a] = new Attribute();
			championdata.m_Characters[1].Attributes[a] = new Attribute();
			championdata.m_Characters[2].Attributes[a] = new Attribute();
			championdata.m_Characters[3].Attributes[a] = new Attribute();
		}
		for (int s = 0 ; s < 4; s++) {
			championdata.m_Characters[0].skills92[s] = new Skill();
			championdata.m_Characters[1].skills92[s] = new Skill();
			championdata.m_Characters[2].skills92[s] = new Skill();
			championdata.m_Characters[3].skills92[s] = new Skill();
		}
	}	
	public void load(String path) {
		/*
		System.out.println(Integer.MAX_VALUE);
		File f = new File(path);
		try {
			// v portraits file ma kazdy charakter 464 bajtov
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			byte[] bytes = new byte[(int)f.length()];
			if (bytes.length == 1404) { // 4 x 319 + 128, portraits su osobitne
				System.out.println("NIE portraits");
			
				for (int i = 0; i < 4; i++)
					championdata.m_Characters[i] = new Champion();
				
				for (int a = 0 ; a < 7; a++) {
					championdata.m_Characters[0].Attributes[a] = new Attribute();
					championdata.m_Characters[1].Attributes[a] = new Attribute();
					championdata.m_Characters[2].Attributes[a] = new Attribute();
					championdata.m_Characters[3].Attributes[a] = new Attribute();
				}
				for (int s = 0 ; s < 4; s++) {
					championdata.m_Characters[0].skills92[s] = new Skill();
					championdata.m_Characters[1].skills92[s] = new Skill();
					championdata.m_Characters[2].skills92[s] = new Skill();
					championdata.m_Characters[3].skills92[s] = new Skill();
				}				
				// citame
				dis.read(bytes);
				ByteBuffer bb = ByteBuffer.wrap(bytes);
				//bb.order(ByteOrder.BIG_ENDIAN);
				bb.order(ByteOrder.LITTLE_ENDIAN);
				//for (int i = 0; i < 4; i++) {
				readChampion(bb,championdata.m_Characters[0]);
				readChampion(bb,championdata.m_Characters[1]);
				readChampion(bb,championdata.m_Characters[2]);
				readChampion(bb,championdata.m_Characters[3]);
				/*
				vypis(championdata.m_Characters[2]);
				vypis(championdata.m_Characters[3]);*/
				
				
			/*	
			}
			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}	*/
		try {
        int pintChampionDataSize = 1404;
        int pintKeyOffset = 10;
        int championportraitsize = 4 * 464;
        boolean toBIG_ENDIAN = true;
        byte[] block1 = new byte[128*2];
        byte[] block2 = new byte[128*2];
        byte[] block3 = new byte[64*2];
        byte[] creatures;
        byte[] champions;
        byte[] timersData;
        byte[] timersQueue;
        byte[] strChampPortraits;
        byte[] strDungData;
      
        DataInputStream dis = new DataInputStream(new FileInputStream(new File(path)));
        dis.read(block1);
        dis.read(block2);
        dis.read(block3);
      
      
        int[] block1_int = new int[block1.length/2];
        int[] block2_int = new int[block2.length/2];
        int[] block3_int = new int[block3.length/2];
        int[] block2de_int = new int[block2.length/2];
        int[] block3de_int = new int[block3.length/2];

        byte[] block2de = new byte[block2.length];
        byte[] block3de = new byte[block3.length];
       
        byteToInt(block1,block1_int);
        byteToInt(block2,block2_int);
        byteToInt(block3,block3_int);

        int block2StoredChecksum = getBlock2StoredChecksum(block1_int);  // checksum block2 je podla block1
        int j = 0;
        System.out.println("key: " + block1_int[pintKeyOffset]);
        int intBlock2ComputedChecksum = Decrypt2(block2_int, block1_int[pintKeyOffset]);
        System.out.println("decrypt intBlock2ComputedChecksum: " + intBlock2ComputedChecksum);
      
      
        intToByte(block2_int,block2de);

        int computedBlock2Checksum = computeBlock2Checksum(block2_int); // checksum AZ PO DECRYPT
        if (block2StoredChecksum != computedBlock2Checksum)
        System.out.println("ERROR block2StoredChecksum ("+block2StoredChecksum+") <>  computedBlock2Checksum ("+computedBlock2Checksum+")");
        
        // Decryption keys and Stored checksum values of subsequent blocks are stored at different offsets in DM and CSB
        int intBlock3Key, intCreaturesDataKey, intChampionDataKey, intTimersDataKey, intTimersQueueKey;
        int intBlock3StoredChecksum, intCreaturesDataStoredChecksum, intChampionDataStoredChecksum, intTimersDataStoredChecksum, intTimersQueueStoredChecksum;
        // pre dm
        intBlock3Key = block2_int[27];
        intCreaturesDataKey = block2_int[28];
        intChampionDataKey = block2_int[29];
        intTimersDataKey = block2_int[30];
        intTimersQueueKey = block2_int[31];
        intBlock3StoredChecksum = block2_int[43];
        intCreaturesDataStoredChecksum = block2_int[44];
        intChampionDataStoredChecksum = block2_int[45];
        intTimersDataStoredChecksum = block2_int[46];
        intTimersQueueStoredChecksum = block2_int[47];
    
        int intBlock3ComputedChecksum = Decrypt2(block3_int, intBlock3Key);
        if (intBlock3StoredChecksum != intBlock3ComputedChecksum)
        System.out.println("ERROR intBlock3StoredChecksum ("+intBlock3StoredChecksum+") <>  intBlock3ComputedChecksum ("+intBlock3ComputedChecksum+")");
            
        intToByte(block3_int,block3de);

        creatures = new byte[block3_int[23] * 8 * 2];
        dis.read(creatures);
        
        int[] creatures_int = new int[creatures.length / 2];
        byteToInt(creatures,creatures_int);
        int intCreaturesDataComputedChecksum = Decrypt2(creatures_int, intCreaturesDataKey);
        if (intCreaturesDataStoredChecksum != intCreaturesDataComputedChecksum)
        System.out.println("ERROR intCreaturesDataStoredChecksum ("+intCreaturesDataStoredChecksum+") <>  intCreaturesDataComputedChecksum ("+intCreaturesDataComputedChecksum+")");

        byte[] creaturesde = new byte[creatures.length];
        intToByte(creatures_int,creaturesde);
        
        champions = new byte[pintChampionDataSize];
        dis.read(champions);
        int[] champions_int = new int[champions.length / 2];
        byteToInt(champions,champions_int);
      //  System.out.println(String.format("%d %d %d %d %d ... %d",champions_int[0], champions_int[1],champions_int[2],champions_int[3],champions_int[4], champions_int[champions_int.length-1]));
        int intChampionDataComputedChecksum = Decrypt2(champions_int, intChampionDataKey);
        if (intChampionDataComputedChecksum != intChampionDataStoredChecksum)
        System.out.println("ERROR intChampionDataComputedChecksum ("+intChampionDataComputedChecksum+") <>  intChampionDataStoredChecksum ("+intChampionDataStoredChecksum+")");
        /*
    	byte[] reds   = new byte[256];
    	byte[] greens = new byte[256];
    	byte[] blues  = new byte[256];
    	
    	for (int i = 0; i < Palettes.PCDMpaletkaRGB.length; i++) {
    		reds[i] = (byte)Palettes.PCDMpaletkaRGB[i][0];
    		greens[i] = (byte)Palettes.PCDMpaletkaRGB[i][1];
    		blues[i] = (byte)Palettes.PCDMpaletkaRGB[i][2];
    	}*/
    	/*
    	  // tato to nie je urcite
    	for (int i = 0; i < Palettes.AmigaDMpaletkaRGB.length; i++) {
    		reds[i] = (byte)Palettes.AmigaDMpaletkaRGB[i][0];
    		greens[i] = (byte)Palettes.AmigaDMpaletkaRGB[i][1];
    		blues[i] = (byte)Palettes.AmigaDMpaletkaRGB[i][2];
    	}    	
    	*/
    	/*
    	for (int i = 0; i < 4; i++) {
    		championdata.m_Characters[i] = new Champion();
    		IndexColorModel icm = new IndexColorModel(8,256,reds, greens, blues);
    		championdata.m_Characters[i].portrait = new BufferedImage(32,29,BufferedImage.TYPE_BYTE_INDEXED, icm);
    	}
    		
    	
    	for (int a = 0 ; a < 7; a++) {
    		championdata.m_Characters[0].Attributes[a] = new Attribute();
    		championdata.m_Characters[1].Attributes[a] = new Attribute();
    		championdata.m_Characters[2].Attributes[a] = new Attribute();
    		championdata.m_Characters[3].Attributes[a] = new Attribute();
    	}
    	for (int s = 0 ; s < 4; s++) {
    		championdata.m_Characters[0].skills92[s] = new Skill();
    		championdata.m_Characters[1].skills92[s] = new Skill();
    		championdata.m_Characters[2].skills92[s] = new Skill();
    		championdata.m_Characters[3].skills92[s] = new Skill();
    	}*/
    	
        
        intToByte(champions_int, champions);
        ByteBuffer bchamp = ByteBuffer.wrap(champions);
        bchamp.order(ByteOrder.LITTLE_ENDIAN);
        readChampion(bchamp, championdata.m_Characters[0]);
        readChampion(bchamp, championdata.m_Characters[1]);
        readChampion(bchamp, championdata.m_Characters[2]);
        readChampion(bchamp, championdata.m_Characters[3]);

        
        byte[] timersdata = new byte[block3_int[14] * 5 * 2 ];
       // System.out.println("timersdata size: " + timersdata.length);
        dis.read(timersdata);
        int[] timersdata_int = new int[block3_int[14] * 5];
        byteToInt(timersdata, timersdata_int);
        
       // System.out.println(String.format("%d %d %d %d %d ... %d",timersdata_int[0], timersdata_int[1],timersdata_int[2],timersdata_int[3],timersdata_int[4], timersdata_int[timersdata_int.length-1]));
        int intTimersDataComputedChecksum = Decrypt2(timersdata_int, intTimersDataKey);
        if (intTimersDataComputedChecksum != intTimersDataStoredChecksum)
        System.out.println("ERROR intTimersDataComputedChecksum ("+intTimersDataComputedChecksum+") <>  intTimersDataStoredChecksum ("+intTimersDataStoredChecksum+")");
        
        
        
        byte[] timersqueue = new byte[block3_int[14] * 2 ];
       // System.out.println("timersdata size: " + timersdata.length);
        dis.read(timersqueue);
        int[] timersqueue_int = new int[block3_int[14]];
        byteToInt(timersqueue, timersqueue_int);
        
       // System.out.println(String.format("%d %d %d %d %d ... %d",timersdata_int[0], timersdata_int[1],timersdata_int[2],timersdata_int[3],timersdata_int[4], timersdata_int[timersdata_int.length-1]));
        int intTimersQueueComputedChecksum = Decrypt2(timersqueue_int, intTimersQueueKey);
        if (intTimersQueueComputedChecksum != intTimersQueueStoredChecksum)
        System.out.println("ERROR intTimersQueueComputedChecksum ("+intTimersQueueComputedChecksum+") <>  intTimersQueueStoredChecksum ("+intTimersQueueStoredChecksum+")");
  
  
        byte[] portraits = new byte[championportraitsize];
        dis.read(portraits);
        
       	ByteBuffer bbport = ByteBuffer.wrap(portraits);
       	loadPortrait(championdata.m_Characters[0],bbport);
       	loadPortrait(championdata.m_Characters[1],bbport);
       	loadPortrait(championdata.m_Characters[2],bbport);
       	loadPortrait(championdata.m_Characters[3],bbport);

     //  	ImageIO.write(championdata.m_Characters[3].portrait, "bmp", new File("d:\\hry\\DM\\amiga ok dm1\\x\\pc\\borisko.bmp"));
        
        //System.out.println(String.format("%d %d %d %d %d ... %d",portraits[0], portraits[1],portraits[2],portraits[3],portraits[4], portraits[portraits.length-1]));

       // DataOutputStream  dos = new DataOutputStream(new FileOutputStream(new File("DMSAVE.DAT-creat")));
        // pre opatovne zakryptovanie staci volat zase Decrypt2
        //Decrypt2(block2_int, block1_int[pintKeyOffset]);
        //intToByte(block2_int,block2de);
        //dos.write(creaturesde);
      //  dos.close();
  //    dos = new DataOutputStream(new FileOutputStream(new File("DMSAVE.DAT-block2o")));
  //    dos.write(block2);
  //      dos.close();
        dis.close();
      }
      catch (Exception e) {
          e.printStackTrace();
      }		
	}
	
	private void loadPortrait(Champion champion, ByteBuffer bbport) {
		int imgx = 0;
		byte pix;
		WritableRaster raster = champion.portrait.getRaster();
		for (int y = 0; y < 29; y++) {
			imgx = 0;
			for (int x = 0; x < 16; x++) {
			    pix = bbport.get();
			    raster.setPixel(imgx++, y, new int[] {(Byte.toUnsignedInt(pix) >> 4) & 15});
			    raster.setPixel(imgx++, y, new int[] {Byte.toUnsignedInt(pix) & 15});
			}
		}
	}
	private void readChampion(ByteBuffer bb, Champion champion) {
		bb.get(champion.name);  // 00
		bb.get(champion.title);// 08 // size??
		champion.wordx24 = bb.getShort(); // Not swapped because I don't see it used.
		bb.get(champion.FILL26);
		champion.facing = bb.get();  //28
		champion.position = bb.get(); //29
		champion.byte30 = bb.get();
		champion.byte31 = bb.get();
		champion.attackType = bb.get(); //32Signed so check for -1 works.
		champion.byte33 = bb.get();
		bb.get(champion.incantation);//34;
		        // [0] = power 96 through 111 --> 1 through 6
		        // [1] =      102 through 107
		        // [2] =      108 through 113
		        // [3] =      114 through 119
		bb.get(champion.FILL38);
		champion.facing3 = bb.get(); //40
		champion.uByte41 = bb.get();
		champion.uByte42 = bb.get(); //Poison count.  A timer for each one??
		champion.uByte43 = bb.get();		
		champion.busyTimer = bb.getShort();// 44 // Timer Index. Signed so -1 works.
		champion.timerIndex = bb.getShort();//46;
		champion.word48 = bb.getShort(); // ORed with 0x280
		              // 0x8000 while selecting attack option
		champion.ouches = bb.getShort();     //50;// mask of damaged body parts.
		champion.HP = bb.getShort();         //52
		champion.maxHP = bb.getShort();      //54
		champion.stamina = bb.getShort();    //56;
		champion.maxStamina = bb.getShort(); //58;
		champion.mana = bb.getShort();       //60;
		champion.maxMana = bb.getShort();    //62;
		champion.word64 = bb.getShort();
		champion.food = bb.getShort();       //66;
		champion.water = bb.getShort();      //68;
		for (int i = 0; i < 7; i++) {// 70 (maximum, current, minimum)
			champion.Attributes[i].ubMaximum = bb.get();
			champion.Attributes[i].ubCurrent = bb.get();
			champion.Attributes[i].ubMinimum = bb.get();
		}
                     // 70 = [0] Luck??
                     // 73 = [1] Strength
                     // 76 = [2] Dexterity
                    // 79 = [3] Wisdom
                    // 82 = [4] Vitality
                     // 85 = [5] AntiMagic
                     // 88 = [6] AntiFire
//		champion.FILL91 = bb.get();
		for (int i = 0; i < 4; i++) {// 92 //0 and 4-7  =Fighter
			champion.skills92[i].word0 = bb.getShort();
			champion.skills92[i].Long2 = bb.getInt();
		}
		              //1 and 8-11 =Ninja
		              //2 and 12-15=Priest
		              //3 and 16-19=Wizard
		//bb.get();
		for (int i = 0; i < 20; i++)
			champion.possessions[i] = bb.getShort();
		  champion.load = bb.getShort();//272; In 10ths of KG
			
		  champion.shieldStrength = bb.getShort(); //274;
		 // bb.get(champion.FILL276);
		//  bb.get(champion.portrait); //336
		 // bb.get(champion.FILL337);
		 for (int i = 0; i < champion.unknown.length; i++)
			 champion.unknown[i] = bb.getShort();
		 // bb.get(champion.unknown);
	
	}
    public static void intToByte(int[] block2_int, byte[] block2de){
        for (int i = 0; i < block2de.length; i+=2){
           block2de[i+1] = (byte)(block2_int[i>>1] >> 8);
           block2de[i] =(byte) (block2_int[i>>1] & 255);
        }
    }
    public static void byteToInt(byte[] block1, int[] block1_int){
        for (int i = 0; i < block1.length; i+=2){
        
            block1_int[i>>1] = Byte.toUnsignedInt(block1[i+1]);
            block1_int[i>>1] <<= 8;
            block1_int[i>>1] ^= Byte.toUnsignedInt(block1[i]);
        
        /*
            // opacne   
            block1_int[i] = Byte.toUnsignedInt(block1[i+1]);
            block1_int[i] <<= 8;
            block1_int[i] ^= Byte.toUnsignedInt(block1[i]);
            */
        } 
    }
    public static int computeBlock2Checksum(int[] arrBlock2){
        int intBlock2ComputedChecksum = 0;
        for (int i = 0; i <= 127; i++)
            intBlock2ComputedChecksum = (intBlock2ComputedChecksum + arrBlock2[i]) % 65536;
        return intBlock2ComputedChecksum;    
    }
    public static int getBlock2StoredChecksum(int[] arrBlock1){
         int intBlock2StoredChecksum = 0;
         int i = 0;
         do {
            intBlock2StoredChecksum = (intBlock2StoredChecksum + arrBlock1[i]) % 65536;
            intBlock2StoredChecksum = intBlock2StoredChecksum ^ arrBlock1[i + 1];
            intBlock2StoredChecksum = (intBlock2StoredChecksum - arrBlock1[i + 2] + 65536) % 65536;
            intBlock2StoredChecksum = intBlock2StoredChecksum ^ arrBlock1[i + 3];
            i = i + 4;
         }
         while (i != 128);  
         return   intBlock2StoredChecksum;
    }
    public static byte[] shortToBytesLITTLE_ENDIAN(short[] section){
        ByteBuffer bb = ByteBuffer.allocate(section.length*2);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < section.length; i++)
           bb.putShort(section[i]);
        byte[] ret = new byte[section.length*2];   
        for (int i = 0; i < section.length*2; i++)
           ret[i] = bb.get(i);
       return ret;
    }
    public static int Decrypt2(int[] arrSection, int key){
      int Decrypt = key;
      int TempValue = key;
      int NumberOfWords = arrSection.length;
      System.out.println("NumberOfWords " + NumberOfWords);
      for (int i = 0; i < NumberOfWords; i++){
         Decrypt = (Decrypt + arrSection[i]) % 65536;
         arrSection[i] = arrSection[i] ^ TempValue;
         Decrypt = (Decrypt + arrSection[i]) % 65536;
         TempValue = (TempValue + NumberOfWords - i) % 65536;
      }
      return Decrypt;
    }
       
    public static int computeintBlock2StoredChecksum(short[] block1s){
      int intBlock2StoredChecksum = 0;
      int i = 0;
      do {
        intBlock2StoredChecksum = (intBlock2StoredChecksum + Short.toUnsignedInt(block1s[i])) % 65536;
        intBlock2StoredChecksum = intBlock2StoredChecksum ^ Short.toUnsignedInt(block1s[i+1]);
        intBlock2StoredChecksum = (intBlock2StoredChecksum - Short.toUnsignedInt(block1s[i+2]) + 65536) % 65536;
        intBlock2StoredChecksum = intBlock2StoredChecksum ^ Short.toUnsignedInt(block1s[i+3]);
        i = i+4;
      } while (i < 128);
      return intBlock2StoredChecksum;
    }
    public static short[] bytesToShortLITTLE_ENDIAN(byte[] bytes){
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (int i = 0; i < bytes.length; i++)
           bb.put(bytes[i]);
        short[] ret = new short[bytes.length/2];
        for (int i = 0; i < bytes.length/2; i++)   
            ret[i] = bb.getShort(i);
        return ret;    
    }	
}
