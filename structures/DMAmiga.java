package structures;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayDeque;


//DM savegame characters transfer from PC savegame to Amiga savegame
//copyright 2025 Ján Mojžiš, janmojzisx@gmail.com, github https://github.com/gensuda/DMTools
//this java port is based on SavedGameDecoder Written by Christophe Fontanel
// actually function Decrypt is a two way function, it can decrypt encrypted data, however, it can do reverse
// if used with unencrypted data.

public class DMAmiga {
	public class ChampionData{
		  // ************* NOTE WELL !! *************
		  // 3328 bytes read into this part of the structure.
		  // This includes the four characters and 128 additional
		  // bytes of global data.
		 public  Champion[] m_Characters = new Champion[4];
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
		public byte[] name = new byte[8];  // 00
		public byte[] title = new byte[16];// 08 // size??
		  short wordx24; // Not swapped because I don't see it used.
		  byte[] FILL26 = new byte[28-26];
		  byte facing;  //28
		  byte position; //29
		  byte byte30;
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
		  byte  FILL91;
		  public Skill[] skills92 = new Skill[4]; // 92 //0 and 4-7  =Fighter
		                            //1 and 8-11 =Ninja
		                            //2 and 12-15=Priest
		                            //3 and 16-19=Wizard
		 // byte[]  possessions = new Rn[30]; //212
		  short[] possessions = new short[30];

		  short load;//272; In 10ths of KG
		  short shieldStrength; //274;
		  byte[] dummy = new byte[176-24+8+8+8];
		  byte[] FILL337 = new byte[464];	// TOTO JE PORTRET, to predtym je nieco ine	rozmer ma byt 64 x 58 = 3712
		  byte[] koniec = new byte[24-8-8-8];
		 public  BufferedImage portrait;
		                                       // v skutocnosti je to 58 riadkov x 8 stlpcov  = 464 ale stlpce su 4 bity na pixel
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

	public ChampionData championdata = new ChampionData();
	byte[] block1;
	byte[] block2;
	byte[] block3;
	byte[] block4_creatures;
	byte[] block5_champions;
	byte[] block6_timersdata; // champion data
	byte[] block7_timersqueue;
	byte[] block8_dungeondata;
	/*
	public void load(String save) {
		File f = new File(save);
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(f));
			byte[] bytes = new byte[(int)f.length()];
			if (bytes.length == 3328) {  // 4 x 800 + 128
				System.out.println("aj portraits");

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
				bb.order(ByteOrder.BIG_ENDIAN);
			//	bb.order(ByteOrder.BIG_ENDIAN);
				//for (int i = 0; i < 4; i++) {
				readChampion(bb,championdata.m_Characters[0]);
				readChampion(bb,championdata.m_Characters[1]);
				readChampion(bb,championdata.m_Characters[2]);
				readChampion(bb,championdata.m_Characters[3]);
			}
			dis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}	*/
	private void writeChampion(ByteBuffer bb, Champion champion) {
		bb.put(champion.name);  // 00
		bb.put(champion.title);// 08 // size??
		bb.putShort(champion.wordx24);
		
		bb.put(champion.FILL26);
		bb.put(champion.facing);
		bb.put(champion.position);
		bb.put(champion.byte30);
		bb.put(champion.byte31);
		bb.put(champion.attackType);
		bb.put(champion.byte33);
		bb.put(champion.incantation);//34;
		        // [0] = power 96 through 111 --> 1 through 6
		        // [1] =      102 through 107
		        // [2] =      108 through 113
		        // [3] =      114 through 119
		bb.put(champion.FILL38);
		bb.put(champion.facing3);
		bb.put(champion.uByte41);
		bb.put(champion.uByte42);
		bb.put(champion.uByte43);		
		bb.putShort(champion.busyTimer);// 44 // Timer Index. Signed so -1 works.
		bb.putShort(champion.timerIndex);//46;
		bb.putShort(champion.word48); // ORed with 0x280
		              // 0x8000 while selecting attack option
		bb.putShort(champion.ouches);     //50;// mask of damaged body parts.
		bb.putShort(champion.HP);         //52
		bb.putShort(champion.maxHP);      //54
		bb.putShort(champion.stamina);    //56;
		bb.putShort(champion.maxStamina); //58;
		bb.putShort(champion.mana);       //60;
		bb.putShort(champion.maxMana);    //62;
		bb.putShort(champion.word64);
		bb.putShort(champion.food);       //66;
		bb.putShort(champion.water);      //68;
		for (int i = 0; i < 7; i++) {// 70 (maximum, current, minimum)
			bb.put(champion.Attributes[i].ubMaximum);
			bb.put(champion.Attributes[i].ubCurrent);
			bb.put(champion.Attributes[i].ubMinimum);
		}
                       // 70 = [0] Luck??
                       // 73 = [1] Strength
                       // 76 = [2] Dexterity
                      // 79 = [3] Wisdom
                      // 82 = [4] Vitality
                       // 85 = [5] AntiMagic
                       // 88 = [6] AntiFire
		bb.put(champion.FILL91);
		for (int i = 0; i < 4; i++) {// 92 //0 and 4-7  =Fighter
			bb.putShort(champion.skills92[i].word0);
			bb.putInt(champion.skills92[i].Long2);
		}
		              //1 and 8-11 =Ninja
		              //2 and 12-15=Priest
		              //3 and 16-19=Wizard
		for (int i = 0; i < 20; i++)
			bb.putShort(champion.possessions[i]);

		bb.putShort(champion.load);//272; In 10ths of KG
		bb.putShort(champion.shieldStrength); //274;
		  /*
		  bb.get(champion.FILL276);
		  bb.get(champion.portrait); //336
		  bb.get(champion.posuvac);
		  */
		  bb.put(champion.dummy);
		//  bb = bb.position(bb.position()+3);
		  encodePortrait(champion);
		  bb.put(champion.FILL337);
		  
		///  bb = bb.position(bb.position()-3);
		  
		  if (champion.koniec.length > 0)
			  bb.put(champion.koniec);			
				
	}
	private void encodePortrait(Champion champion) {
		// ulozit portret do FILL337
		// zobere 32 pixelov
		//byte[] out = champion.FILL337;
		//Arrays.fill(out, (byte)0);
		ByteBuffer encoded = ByteBuffer.allocate(champion.FILL337.length); // aby sme mohli rovno zapisovat LONG
		//encoded.order(ByteOrder.LITTLE_ENDIAN);
		encoded.order(ByteOrder.BIG_ENDIAN);
		WritableRaster pixels = champion.portrait.getRaster();
		int x = 0;
		int[] pIndex = new int[1];
		for (int y = 0; y < 29; y++) {
	        for (int plane = 0; plane < 4; plane++) {
	            short word = 0;
            x = 0;
            for (int i = 0; i < 16; i++) {
            	pixels.getPixel(x+i, y, pIndex);
                int pixel = pIndex[0];// pixels16[i+x] & 0xF; // ensure 4-bit
                int bit = (pixel >> plane) & 1;
                word |= (bit << (15 - i)); // MSB = leftmost pixel
            }
            encoded.putShort(word);
            word = 0;
	        }
        for (int plane = 0; plane < 4; plane++) {
            short word = 0;
		
            x = 16;
            for (int i = 0; i < 16; i++) {
            	pixels.getPixel(x+i, y, pIndex);
                int pixel = pIndex[0];// pixels16[i+x] & 0xF; // ensure 4-bit
                int bit = (pixel >> plane) & 1;
                word |= (bit << (15 - i)); // MSB = leftmost pixel
            }
            // Write big-endian word: high byte first
            //output[outIndex++] = (byte) ((word >> 8) & 0xFF);
            //output[outIndex++] = (byte) (word & 0xFF);
            encoded.putShort(word);
        }
		
        }		
		encoded.position(0);
		encoded.get(champion.FILL337);
		/*
		System.out.println("velkost pred:\t"+champion.FILL337.length);
		System.out.println("velkost po:\t"+encoded.capacity());
		System.out.print("pred:\t");
		for (int i = 0; i < 40; i++)
			System.out.print(String.format("%x ", champion.FILL337[i]));
		System.out.println();
		System.out.print("po:\t");
		for (int i = 0; i < 40; i++)
		System.out.print(String.format("%x ", encoded.get()));
		System.out.println();*/
		/*
		long pixel16 = 0;
		int x = 0;
		
		for (int y = 0; y < 29; y++) {
			// najprv 16 pixlov
			// potom zae 16 pixlov
			x = 0;
			for (int a = 0; a < 2; a++) {
				for (int x = 0; x < 16; x++) { // 0-15
					pixels.getPixel(x, y, pIndex);
					pixel16 ^=  pIndex[0] & 1;  // bit 0
				}
				
				x+= 16;
				
			}
		}*/
		
	}
	private void readChampion(ByteBuffer bb, Champion champion) {
		bb.order(ByteOrder.BIG_ENDIAN);
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
		champion.FILL91 = bb.get();
		for (int i = 0; i < 4; i++) {// 92 //0 and 4-7  =Fighter
			champion.skills92[i].word0 = bb.getShort();
			champion.skills92[i].Long2 = bb.getInt();
		}
		              //1 and 8-11 =Ninja
		              //2 and 12-15=Priest
		              //3 and 16-19=Wizard
		for (int i = 0; i < 20; i++)
			champion.possessions[i] = bb.getShort();

		  champion.load = bb.getShort();//272; In 10ths of KG
		  champion.shieldStrength = bb.getShort(); //274;
		  /*
		  bb.get(champion.FILL276);
		  bb.get(champion.portrait); //336
		  bb.get(champion.posuvac);
		  */
		  bb.get(champion.dummy);
		//  bb = bb.position(bb.position()+3);
		  bb.get(champion.FILL337);
		  decodePortrait(champion);
//		  ImageIO.write(champion.portrait, null, null)
		///  bb = bb.position(bb.position()-3);
		  
		  if (champion.koniec.length > 0)
			  bb.get(champion.koniec);			
		
	}	
	private void decodePortrait(Champion champion) {
			// zhora zlava -> napravo dole
			// 58 riadkov x 8 stlpcov kazdy po 4 bity
			//int riadkov = 58;
			byte[] riadok8 = new byte[8]; // komprimovane 4 bity / pixel
			//byte[] riadok16 = new byte[16]; // dekomprimovane  8 bitov / pixel
			ByteBuffer bb = ByteBuffer.wrap(champion.FILL337);
		
			BufferedImage bi = champion.portrait;
			
			int bits4_0, bits4_1;
			int p = 0;
			int bit;
			int y;
			int sx = 0;
			StringBuilder sb = new StringBuilder();
			//ArrayDeque<int[]> pixle_2_riadky = new ArrayDeque<int[]>();
			ArrayDeque<byte[]> pixle_2_riadky = new ArrayDeque<byte[]>();
			short p16;
			int decodedlength = 0;
			ArrayDeque<byte[]> debugout = new ArrayDeque<byte[]>();
			
			for (int i = 0;  i < 58; i++) {
				y = i /2;
				//int[] pixels = new int[16];
				byte[] pixels = new byte[16];
				for (int j = 0; j < 4; j++) {
				  //p16 = Short.toUnsignedInt(bb.getShort());
				  p16 = bb.getShort();
				  
				  for (int k = 0; k < 16; k++) {
					  bit = ((p16 >> 15-k)&1);
	  			      pixels[k] ^= ((p16 >> 15-k)&1) << j;
				  }
				}
			    pixle_2_riadky.add(pixels);

			    if (pixle_2_riadky.size() == 2) {
			    	sb.append("   ");
				       pixels = pixle_2_riadky.removeFirst();
				       decodedlength+=pixels.length;
				       debugout.add(pixels);
					   for (int x = 0; x < 16; x++) {
							 bi.getRaster().setPixel(x, y, /*new int[] {pixels[x]*15}*/ new int[] {Byte.toUnsignedInt(pixels[x])});
						  //bi.getRaster().setPixel(x, y, /*new int[] {pixels[x]*15}*/ /*amigaDMpaletkaRGB[pixels[x]]*/ new int[]{255,255,0});
						   //bi.getRaster().setDataElements(x, y, /*amigaDMpaletkaRGB[pixels[x]]*/ new byte[]{(byte)255,(byte)255,0});
						  sb.append(String.format("%02x ",  pixels[x]));
					  }
			       pixels = pixle_2_riadky.removeFirst();
			       decodedlength+=pixels.length;
			       debugout.add(pixels);
			       int[] pixelIndexed = new int[1];
				   for (int x = 0; x < 16; x++) {
						 bi.getRaster().setPixel(x+16, y, /*new int[] {pixels[x]*15}*/ new int[] {Byte.toUnsignedInt(pixels[x])});
					  //bi.getRaster().setPixel(x+16, y, /*new int[] {pixels[x]*15}*/ /*amigaDMpaletkaRGB[pixels[x]]*/ new int[] {255,255,0});
					   //bi.getRaster().setDataElements(x+16, y, /*amigaDMpaletkaRGB[pixels[x]]*/ new byte[]{(byte)255,(byte)255,0});
					   
	   			      //bi.getColorModel().getDataElements(new Color(), pixels)
					  sb.append(String.format("%02x ",  pixels[x]));
				  }
			       //bi.getRaster().setDataElements(0, y, pixels);
				   sb.append("\r\n");
				   
			    }
			}
		//	System.out.println(" portrait, original size " + portrait_464.length + " (58x8), decoded " + decodedlength + " (32x29)" );
		//	System.out.println(sb.toString());
			

					
	}
	
	public void save(String save) {
		
		/*
		ByteBuffer bb = ByteBuffer.allocate(3328);
		bb.order(ByteOrder.BIG_ENDIAN);
		writeChampion(bb,championdata.m_Characters[0]);
		writeChampion(bb,championdata.m_Characters[1]);
		writeChampion(bb,championdata.m_Characters[2]);
		writeChampion(bb,championdata.m_Characters[3]);*/
		// TODO Auto-generated method stub
		int pintKeyOffset = 10;  // = Amiga DM a PC DM
		int pintChampionDataSize = 3328; // LEN Amiga DM, PC DM = 1404 
		prehodDvojice(block1);
		byte[] block1_byte = block1;
		prehodDvojice(block2);
		byte[] block2_byte = block2;
		prehodDvojice(block3);
		byte[] block3_byte = block3;
		
		
		prehodDvojice(block4_creatures);
		//bbch.order(ByteOrder.BIG_ENDIAN);
		//bbch.order(ByteOrder.LITTLE_ENDIAN);

		prehodDvojice(block6_timersdata);
		prehodDvojice(block7_timersqueue);
		
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(new File(save)));
			//DataInputStream dis = new DataInputStream(new FileInputStream(f_block1));
			//dis.read(block1_byte);
			//dis.close();
			//dis = new DataInputStream(new FileInputStream(f_block2));
			//dis.read(block2_byte);
			//dis.close();
			//dis = new DataInputStream(new FileInputStream(f_block3));
			//dis.read(block3_byte);
			//dis.close();
			
			int[] block1_int = new int[block1_byte.length/2];
			int[] block2_int = new int[block2_byte.length/2];
			int[] block3_int = new int[block3_byte.length/2];
			byteToInt(block1_byte,block1_int);
			byteToInt(block2_byte,block2_int);
			byteToInt(block3_byte,block3_int);
			
			
			int intBlock3Key, intCreaturesDataKey, intChampionDataKey, intTimersDataKey, intTimersQueueKey;
			int intBlock3StoredChecksum, intCreaturesDataStoredChecksum, intChampionDataStoredChecksum, intTimersDataStoredChecksum, intTimersQueueStoredChecksum;
	         // pre dm, block2 musi byt este decryptovany
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
	        
			// zvysne casti, CHAMPIONS --------------------------------------------------------------------
	ByteBuffer bbch = ByteBuffer.allocate(block5_champions.length);
	writeChampion(bbch,championdata.m_Characters[0]);		
	writeChampion(bbch,championdata.m_Characters[1]);		
	writeChampion(bbch,championdata.m_Characters[2]);		
	writeChampion(bbch,championdata.m_Characters[3]);	
			
	bbch.position(0);
	bbch.get(block5_champions);
			
	prehodDvojice(block5_champions);
    int[] champions_int = new int[block5_champions.length / 2];
    byteToInt(block5_champions,champions_int);
    int intChampionDataComputedChecksum = Decrypt2(champions_int, intChampionDataKey);
    block2_int[45] = intChampionDataComputedChecksum;
 //   		if (intChampionDataStoredChecksum != intChampionDataComputedChecksum)
  //  			System.out.println("ERROR intChampionDataStoredChecksum ("+intChampionDataStoredChecksum+") <>  intChampionDataComputedChecksum ("+intChampionDataComputedChecksum+")");

	byte[] out_champions = new byte[champions_int.length*2];
	intToByte(champions_int, out_champions);
	      
	        
			int computedBlock2Checksum = computeBlock2Checksum(block2_int); // block2 checksum PRED encrypt
			int intBlock2ComputedChecksum = Decrypt2(block2_int, block1_int[pintKeyOffset]);
			int block2StoredChecksum = 0;
			block1_int[0] = 0;
	while ((block2StoredChecksum = getBlock2StoredChecksum(block1_int)) != computedBlock2Checksum){		
		block1_int[0]++;
	};
	block2StoredChecksum =  getBlock2StoredChecksum(block1_int);
	      //        System.out.println("ERROR block2StoredChecksum ("+block2StoredChecksum+") <>  computedBlock2Checksum ("+computedBlock2Checksum+")");
			
			
			byte[] creatures =  block4_creatures;//new byte[block3_int[23] * 8 * 2]; // block3 musi byt decryptovany
		//File f_creatures = new File(path+"DMGame.dat-05-CreaturesData.bin");
			//byte[] creatures_byte = new byte[/*(int)f_creatures.length()*/];
			//System.out.println("creatures size: "+creatures.length);
			//dis = new DataInputStream(new FileInputStream(f_creatures));
			//dis.read(creatures);
			//dis.close();
			
//			byte[] champions = block5_champions; //new byte[pintChampionDataSize]; // block3 musi byt decryptovany
			//File f_champions = new File(path+"DMGame.dat-06-ChampionData.bin");
			//dis = new DataInputStream(new FileInputStream(f_champions));
			//dis.read(champions);
			//dis.close();

			byte[] timers = block6_timersdata; //new byte[block3_int[14] * 5 * 2]; // block3 musi byt decryptovany
			//File f_timers = new File(path+"DMGame.dat-07-TimersData.bin");
			//dis = new DataInputStream(new FileInputStream(f_timers));
			//dis.read(timers);
			//dis.close();

			byte[] timersqueue = block7_timersqueue; //new byte[block3_int[14] * 2]; // block3 musi byt decryptovany
			//File f_timersqueue = new File(path+"DMGame.dat-08-TimersQueue.bin");
			//dis = new DataInputStream(new FileInputStream(f_timersqueue));
			//dis.read(timersqueue);
			//dis.close();

			// champion portraits neni
			
			
			// zvysne casti, CREATURES --------------------------------------------------------------------
	        int[] creatures_int = new int[creatures.length / 2];
	        byteToInt(creatures,creatures_int);
	        int intCreaturesDataComputedChecksum = Decrypt2(creatures_int, intCreaturesDataKey);
	        if (intCreaturesDataStoredChecksum != intCreaturesDataComputedChecksum)
	        System.out.println("ERROR intCreaturesDataStoredChecksum ("+intCreaturesDataStoredChecksum+") <>  intCreaturesDataComputedChecksum ("+intCreaturesDataComputedChecksum+")");

	        byte[] out_creatures = new byte[creatures_int.length*2];
			intToByte(creatures_int, out_creatures);

			
			byte[] out_block2 = new byte[block2_int.length*2];
		//	block2_int[45] = intChampionDataComputedChecksum;
			intToByte(block2_int, out_block2);
			// zvysne casti, TIMERS --------------------------------------------------------------------
	        int[] timers_int = new int[timers.length / 2];
	        byteToInt(timers,timers_int);
	        int intTimersDataComputedChecksum = Decrypt2(timers_int, intTimersDataKey);
	        if (intTimersDataStoredChecksum != intTimersDataComputedChecksum)
	        System.out.println("ERROR intTimersDataStoredChecksum ("+intTimersDataStoredChecksum+") <>  intTimersDataComputedChecksum ("+intTimersDataComputedChecksum+")");

	        byte[] out_timers = new byte[timers_int.length*2];
			intToByte(timers_int, out_timers);
			
			// zvysne casti, TIMERS QUEUE----------------------------------------------------------------
	        int[] timersqueue_int = new int[timersqueue.length / 2];
	        byteToInt(timersqueue,timersqueue_int);
	        int intTimersQueueComputedChecksum = Decrypt2(timersqueue_int, intTimersQueueKey);
	        if (intTimersQueueStoredChecksum != intTimersQueueComputedChecksum)
	        System.out.println("ERROR intTimersQueueStoredChecksum ("+intTimersQueueStoredChecksum+") <>  intTimersQueueComputedChecksum ("+intTimersQueueComputedChecksum+")");

	        byte[] out_timersqueue = new byte[timersqueue_int.length*2];
			intToByte(timersqueue_int, out_timersqueue);
			
			// champion portraits neni
			
			// prilep dungeondata
			//File f_dungeondata = new File(path+"DMGame.dat-10-DungeonData.bin");
			byte[] dungeondata = block8_dungeondata; //new byte[(int)f_dungeondata.length()];
			//dis = new DataInputStream(new FileInputStream(f_dungeondata));
			//dis.read(dungeondata);
			//dis.close();
			
			
			
			
			//dis = new DataInputStream(new FileInputStream(f_block3));
			//dis.read(block3_byte);
			//dis.close();
			
			
			
            int intBlock3ComputedChecksum = Decrypt2(block3_int, intBlock3Key);
	        if (intBlock3StoredChecksum != intBlock3ComputedChecksum)
	        System.out.println("ERROR intBlock3StoredChecksum ("+intBlock3StoredChecksum+") <>  intBlock3ComputedChecksum ("+intBlock3ComputedChecksum+")");
			byte[] out_block3 = new byte[block3_int.length*2];
			intToByte(block3_int, out_block3);
			
			intToByte(block1_int, block1_byte);
			prehodDvojice(block1_byte);
			prehodDvojice(out_block2);
			prehodDvojice(out_block3);
			prehodDvojice(out_creatures);
			prehodDvojice(out_champions);
			prehodDvojice(out_timers);
			prehodDvojice(out_timersqueue);
	 	          
			dos.write(block1_byte);
			dos.write(out_block2);
			dos.write(out_block3);
			dos.write(out_creatures);
			dos.write(out_champions);
			dos.write(out_timers);
			dos.write(out_timersqueue);
			dos.write(dungeondata);
			// champion portraits neni
			dos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}		
		
		
	}
	public void load(String save) {
		try {
		    DataOutputStream  dos = new DataOutputStream(new FileOutputStream(new File("d:\\hry\\DM\\amiga ok dm1\\debugb1.bin")));
	        ByteBuffer bbout; 
		int pintChampionDataSize = 3328;
        int pintKeyOffset = 10;
        block1 = new byte[128*2];
        block2 = new byte[128*2];
        block3 = new byte[64*2];
        byte[] creatures4;
        byte[] champions5;
        byte[] timersData6;
        byte[] timersQueue7;
        byte[] strChampPortraits8;
        byte[] strDungData9;
        File f = new File(save);
        byte[] input = new byte[(int)f.length()];
        
        DataInputStream dis = new DataInputStream(new FileInputStream(f));
        dis.read(input);
        dis.close();
        ByteBuffer bb = ByteBuffer.wrap(input);
        // bb.order(ByteOrder.BIG_ENDIAN);
        
        short tmp;
        
      // bb.order(ByteOrder.LITTLE_ENDIAN);
        
        bb.get(block1);
        prehodDvojice(block1);
        // block1 je dobre nacitany
        /*
        bbout = ByteBuffer.wrap(block1);
        bbout.order(ByteOrder.LITTLE_ENDIAN);
        byte[] dummy = new byte[block1.length];
        bbout.get(dummy);
        dos.write(dummy);
        dos.close();
        */
        
        //bb.get(block1);
        bb.get(block2);
        prehodDvojice(block2);
        bb.get(block3);
        prehodDvojice(block3);
      
      
        int[] block1_int = new int[block1.length/2];
        int[] block2_int = new int[block2.length/2];
        int[] block3_int = new int[block3.length/2];
      //  int[] block2de_int = new int[block2.length/2];
       // int[] block3de_int = new int[block3.length/2];

        //byte[] block2de = new byte[block2.length];
        byte[] block3de = new byte[block3.length];
       
        byteToInt(block1,block1_int);
        byteToInt(block2,block2_int);
        byteToInt(block3,block3_int);

        int block2StoredChecksum = getBlock2StoredChecksum(block1_int);  // checksum block2 je podla block1
        System.out.println("key: " + block1_int[pintKeyOffset]);
        
        int computedBlock2Checksum = Decrypt2(block2_int, block1_int[pintKeyOffset]);
       // System.out.println("decrypt intBlock2ComputedChecksum: " + intBlock2ComputedChecksum);
       // intToByte(block2_int,block2de);
        
      
     //   intToByte(block2_int,block2de);

        computedBlock2Checksum = computeBlock2Checksum(block2_int); // checksum AZ PO DECRYPT
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

        creatures4 = new byte[block3_int[23] * 8 * 2];
        bb.get(creatures4);
        prehodDvojice(creatures4);
        
        int[] creatures_int = new int[creatures4.length / 2];
        byteToInt(creatures4,creatures_int);
        int intCreaturesDataComputedChecksum = Decrypt2(creatures_int, intCreaturesDataKey);
        if (intCreaturesDataStoredChecksum != intCreaturesDataComputedChecksum)
        System.out.println("ERROR intCreaturesDataStoredChecksum ("+intCreaturesDataStoredChecksum+") <>  intCreaturesDataComputedChecksum ("+intCreaturesDataComputedChecksum+")");
       
        champions5 = new byte[pintChampionDataSize];
        bb.get(champions5);
        prehodDvojice(champions5);
        int[] champions5_int = new int[pintChampionDataSize/2];
        byteToInt(champions5, champions5_int);
        
        
        int intChampionDataComputedChecksum = Decrypt2(champions5_int, intChampionDataKey);
        if (intChampionDataStoredChecksum != intChampionDataComputedChecksum)
        System.out.println("ERROR intChampionDataStoredChecksum ("+intChampionDataStoredChecksum+") <>  intChampionDataComputedChecksum ("+intChampionDataComputedChecksum+")");
      
        byte[] timersdata = new byte[block3_int[14] * 5 *2];
        bb.get(timersdata);
        prehodDvojice(timersdata);
        int[] timersdata_int = new int[timersdata.length / 2];
        byteToInt(timersdata, timersdata_int);
        
        int intTimersDataComputedChecksum = Decrypt2(timersdata_int, intTimersDataKey);
        if (intTimersDataComputedChecksum != intTimersDataStoredChecksum)       
        System.out.println("ERROR intTimersDataStoredChecksum ("+intTimersDataStoredChecksum+") <>  intTimersDataComputedChecksum ("+intTimersDataComputedChecksum+")");
        
        
        byte[] arrTimersQueue = new byte[(block3_int[14])*2];
        bb.get(arrTimersQueue);
        prehodDvojice(arrTimersQueue);
        int[] arrTimersQueue_int = new int[arrTimersQueue.length/2];
        byteToInt(arrTimersQueue,arrTimersQueue_int);
        
        int intTimersQueueChecksum = Decrypt2(arrTimersQueue_int, intTimersQueueKey);
        if (intTimersQueueChecksum != intTimersQueueStoredChecksum)       
        System.out.println("ERROR intTimersQueueStoredChecksum ("+intTimersQueueStoredChecksum+") <>  intTimersQueueChecksum ("+intTimersQueueChecksum+")");

        byte[] dungeondata = new byte[bb.capacity()-bb.position()];
        bb.get(dungeondata);
        
        
    	
        prehodDvojice(block1);
        /*
        FileOutputStream fos = new FileOutputStream(new File("d:\\hry\\DM\\amiga ok dm1\\x\\block1.bin"));
        fos.write(block1);
        fos.close();
        */
        
        intToByte(block2_int,block2);
        prehodDvojice(block2);
        /*
        fos = new FileOutputStream(new File("d:\\hry\\DM\\amiga ok dm1\\x\\block2.bin"));
        fos.write(block2);
        fos.close();      
        */  
        
        prehodDvojice(block3de);
        block3 = block3de;
        /*
        fos = new FileOutputStream(new File("d:\\hry\\DM\\amiga ok dm1\\x\\block3.bin"));
        fos.write(block3de);
        fos.close();        
*/
        
        byte[] creaturesde = new byte[creatures4.length];
        intToByte(creatures_int,creaturesde);        
        prehodDvojice(creaturesde);
        block4_creatures = creaturesde;
        /*
        fos = new FileOutputStream(new File("d:\\hry\\DM\\amiga ok dm1\\x\\creatures.bin"));
        fos.write(creaturesde);
        fos.close();
        */
        
        allocateChampions();
        intToByte(champions5_int, champions5);
        prehodDvojice(champions5);
        
    	block5_champions = champions5;
        
        ByteBuffer bb2 = ByteBuffer.wrap(champions5);
        readChampion(bb2,championdata.m_Characters[0]);
        readChampion(bb2,championdata.m_Characters[1]);
        readChampion(bb2,championdata.m_Characters[2]);
        readChampion(bb2,championdata.m_Characters[3]);
        // musime nacitat aj ten prekliaty zvysok, ak je
        if (bb2.position() < bb2.capacity()) {
        	championdata.m_Characters[3].koniec = new byte[bb2.capacity()-bb2.position()];
        	bb2.get(championdata.m_Characters[3].koniec);
        }
      //  System.out.println(bb2.position());
        /*
        ImageIO.write(championdata.m_Characters[0].portrait, "bmp", new File("d:\\hry\\DMCSBPortraitEditr\\p1.bmp"));
        ImageIO.write(championdata.m_Characters[1].portrait, "bmp", new File("d:\\hry\\DMCSBPortraitEditr\\p2.bmp"));
        ImageIO.write(championdata.m_Characters[2].portrait, "bmp", new File("d:\\hry\\DMCSBPortraitEditr\\p3.bmp"));
        ImageIO.write(championdata.m_Characters[3].portrait, "bmp", new File("d:\\hry\\DMCSBPortraitEditr\\p4.bmp"));*/
        /*
        fos = new FileOutputStream(new File("d:\\hry\\DM\\amiga ok dm1\\x\\champions.bin"));
        fos.write(champions5);
        fos.close();*/
        
        intToByte(timersdata_int, timersdata);
        prehodDvojice(timersdata);
    	block6_timersdata = timersdata;
    	/*
        fos = new FileOutputStream(new File("d:\\hry\\DM\\amiga ok dm1\\x\\timersdata.bin"));
        fos.write(timersdata);
        fos.close();      */  
        
        intToByte(arrTimersQueue_int, arrTimersQueue);
        prehodDvojice(arrTimersQueue);

    	block7_timersqueue = arrTimersQueue;
        
    	/*
        fos = new FileOutputStream(new File("d:\\hry\\DM\\amiga ok dm1\\x\\timersqueue.bin"));
        fos.write(arrTimersQueue);
        fos.close();   */             

        block8_dungeondata = dungeondata;
        /*
        fos = new FileOutpu)tStream(new File("d:\\hry\\DM\\amiga ok dm1\\x\\dungeondata.bin"));        
        fos.write(dungeondata);
        fos.close();    */                

        
      }
      catch (Exception e) {
          e.printStackTrace();
     }		
}
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
private void prehodDvojice(byte[] block) {
		byte tmp;
		for (int i = 0; i < block.length; i+=2) {
			tmp = block[i];
			block[i] = block[i+1];
			block[i+1] = tmp;
		}
			
		
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
private  int computeBlock2Checksum(int[] arrBlock2){
    int intBlock2ComputedChecksum = 0;
    for (int i = 0; i <= 127; i++)
        intBlock2ComputedChecksum = (intBlock2ComputedChecksum + arrBlock2[i]) % 65536;
    return intBlock2ComputedChecksum;    
}
private  int getBlock2StoredChecksum(int[] arrBlock1){
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
private  byte[] shortToBytesLITTLE_ENDIAN(short[] section){
    ByteBuffer bb = ByteBuffer.allocate(section.length*2);
    bb.order(ByteOrder.LITTLE_ENDIAN);
    for (int i = 0; i < section.length; i++)
       bb.putShort(section[i]);
    byte[] ret = new byte[section.length*2];   
    for (int i = 0; i < section.length*2; i++)
       ret[i] = bb.get(i);
   return ret;
}
private  int Decrypt2(int[] arrSection, int key){
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
   
private  int computeintBlock2StoredChecksum(short[] block1s){
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
private  short[] bytesToShortLITTLE_ENDIAN(byte[] bytes){
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
