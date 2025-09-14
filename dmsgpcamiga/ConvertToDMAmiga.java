package dmsgpcamiga;

// DM savegame transfer from PC savegame to Amiga savegame.
// Demonstration program, copyright 2025 Ján Mojžiš, janmojzisx@gmail.com, github https://github.com/gensuda/DMTools
// You have to supply existing DM PC savegame and existing DM Amiga savegame in order to
// successfully transfer the characters.
// This program transfers all statistics (HP, Stamina, Mana, attributes including luck, skill levels and also a portrait)


import structures.DMAmiga;
import structures.DMPC;


public class ConvertToDMAmiga {

	public static void main(String[] args) {
		// 1. nacitat DMAmiga save
		// 2. nacitat DMPC save
		
		// 3. vlozit DMPC udaje postav do nacitanej DMAmiga
		// 4. ulozit DMAmiga
		
		// nacitat DMAmiga save
		
		if (args.length < 2) {System.err.println("error, need 3 args:\r\n  1. INPUT DM PC savegame,\r\n  2. INPUT DM Amiga savegame,\r\n  3. OUTPUT DM Amiga savegame"); return;}
	
		DMPC dmpc = new DMPC(); // load DM PC savegame
		dmpc.load(/*"d:\\hry\\DM\\amiga ok dm1\\x\\pc\\dmsave.dat"*/ args[0]);
	
		DMAmiga dmamiga = new DMAmiga(); // load DM amiga savegame
		dmamiga.load(/*"d:\\hry\\DM\\amiga ok dm1\\x\\DMGame.dat"*/ args[1]);	// nacita save, naplni champion statistiky a portret do buffered image
		
		//z PC prenesieme statistiky
		
		//   HP,  STAMINA,  MANA
		//   LUCK, STR, DEX,  VIT, WIS, ANTIMAGIC, ANTIFIRE 
		//   LEVELS
		//   PORTRAITS
		 
		
		prenes(dmamiga, dmpc); // transfer characters DM PC savegame -> AM amiga savegame 
		
		// write transfered characters into DM Amiga savegame 
		dmamiga.save(/*"d:\\hry\\DM\\amiga ok dm1\\x\\DMGame2.dat"*/args[2]); // ulozi, zapise champion statistiky a aj portret z buff image zakoduje

	}

	private static void prenes(DMAmiga dmamiga, DMPC dmpc) {
		for (int i = 0; i < 4; i++) {
			dmamiga.championdata.m_Characters[i].name = dmpc.championdata.m_Characters[i].name;
			dmamiga.championdata.m_Characters[i].title = dmpc.championdata.m_Characters[i].title;
			
			dmamiga.championdata.m_Characters[i].HP = dmpc.championdata.m_Characters[i].HP;
			dmamiga.championdata.m_Characters[i].maxHP = dmpc.championdata.m_Characters[i].maxHP;
			dmamiga.championdata.m_Characters[i].stamina = dmpc.championdata.m_Characters[i].stamina;
			dmamiga.championdata.m_Characters[i].maxStamina = dmpc.championdata.m_Characters[i].maxStamina;
			dmamiga.championdata.m_Characters[i].mana = dmpc.championdata.m_Characters[i].mana;
			dmamiga.championdata.m_Characters[i].maxMana = dmpc.championdata.m_Characters[i].maxMana;
			
			dmamiga.championdata.m_Characters[i].food = dmpc.championdata.m_Characters[i].food;
			dmamiga.championdata.m_Characters[i].water = dmpc.championdata.m_Characters[i].water;
			
			for (int a = 0 ; a < 7; a++) {
				dmamiga.championdata.m_Characters[0].Attributes[a].ubCurrent = dmpc.championdata.m_Characters[0].Attributes[a].ubCurrent;
				dmamiga.championdata.m_Characters[0].Attributes[a].ubMaximum = dmpc.championdata.m_Characters[0].Attributes[a].ubMaximum;
				dmamiga.championdata.m_Characters[0].Attributes[a].ubMinimum = dmpc.championdata.m_Characters[0].Attributes[a].ubMinimum;

				dmamiga.championdata.m_Characters[1].Attributes[a].ubCurrent = dmpc.championdata.m_Characters[1].Attributes[a].ubCurrent;
				dmamiga.championdata.m_Characters[1].Attributes[a].ubMaximum = dmpc.championdata.m_Characters[1].Attributes[a].ubMaximum;
				dmamiga.championdata.m_Characters[1].Attributes[a].ubMinimum = dmpc.championdata.m_Characters[1].Attributes[a].ubMinimum;
				
				dmamiga.championdata.m_Characters[2].Attributes[a].ubCurrent = dmpc.championdata.m_Characters[2].Attributes[a].ubCurrent;
				dmamiga.championdata.m_Characters[2].Attributes[a].ubMaximum = dmpc.championdata.m_Characters[2].Attributes[a].ubMaximum;
				dmamiga.championdata.m_Characters[2].Attributes[a].ubMinimum = dmpc.championdata.m_Characters[2].Attributes[a].ubMinimum;
				
				dmamiga.championdata.m_Characters[3].Attributes[a].ubCurrent = dmpc.championdata.m_Characters[3].Attributes[a].ubCurrent;
				dmamiga.championdata.m_Characters[3].Attributes[a].ubMaximum = dmpc.championdata.m_Characters[3].Attributes[a].ubMaximum;
				dmamiga.championdata.m_Characters[3].Attributes[a].ubMinimum = dmpc.championdata.m_Characters[3].Attributes[a].ubMinimum;
				
			}
			for (int s = 0 ; s < 4; s++) {
				dmamiga.championdata.m_Characters[0].skills92[s].Long2 = dmpc.championdata.m_Characters[0].skills92[s].Long2;  
				dmamiga.championdata.m_Characters[0].skills92[s].word0 = dmpc.championdata.m_Characters[0].skills92[s].word0;  

				dmamiga.championdata.m_Characters[1].skills92[s].Long2 = dmpc.championdata.m_Characters[1].skills92[s].Long2;  
				dmamiga.championdata.m_Characters[1].skills92[s].word0 = dmpc.championdata.m_Characters[1].skills92[s].word0;
				
				dmamiga.championdata.m_Characters[2].skills92[s].Long2 = dmpc.championdata.m_Characters[2].skills92[s].Long2;  
				dmamiga.championdata.m_Characters[2].skills92[s].word0 = dmpc.championdata.m_Characters[2].skills92[s].word0;
				
				dmamiga.championdata.m_Characters[3].skills92[s].Long2 = dmpc.championdata.m_Characters[3].skills92[s].Long2;  
				dmamiga.championdata.m_Characters[3].skills92[s].word0 = dmpc.championdata.m_Characters[3].skills92[s].word0;  
			}
			dmamiga.championdata.m_Characters[0].portrait = dmpc.championdata.m_Characters[0].portrait; 			
			dmamiga.championdata.m_Characters[1].portrait = dmpc.championdata.m_Characters[1].portrait; 			
			dmamiga.championdata.m_Characters[2].portrait = dmpc.championdata.m_Characters[2].portrait; 			
			dmamiga.championdata.m_Characters[3].portrait = dmpc.championdata.m_Characters[3].portrait; 			
			
		}		
	}

}
