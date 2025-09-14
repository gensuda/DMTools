package structures;

// CM savegame transfer from PC savegame to Amiga savegame
// copyright 2025 Ján Mojžiš, janmojzisx@gmail.com, github https://github.com/gensuda/DMTools

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