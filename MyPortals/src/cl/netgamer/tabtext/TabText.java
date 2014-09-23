package cl.netgamer.tabtext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**<pre>
 * TabText: class to write column formatted text in minecraft chat area
 * 
 * - it splits each field and trims or fill with spaces to adjust tabs
 * 
 * general usage example:
 * 
 * - create a multiline string similar to csv format and create a TabText object with it
 * - use line feed "\n" (ascii 10) as line separator and grave accent "`" (ascii 96) as field separator
 * - you can use some format codes, see http://minecraft.gamepedia.com/Formatting_codes
 * - DO NOT USE LOWERCASE CODES OR BOLD FORMAT BECAUSE IT CAN BREAK SPACING
 * 
 * - // example
 * - multilineString  = "PLAYER------`RATE------`RANK------\n";
 * - multilineString += "§EJohn`10.01`1§R\n";
 * - multilineString += "Doe`-9.30`2";
 * 
 * - TabText tt = new TabText(multilineString);
 * - int numPages = tt.setPageHeight(pageHeight); // set page height and get number of pages
 * - tt.setTabs(10, 18, ...); // horizontal tabs positions, considering 6px chars
 * - tt.sortByFields(-2, 1); // sort by second column descending, then by first
 * - printedText = tt.getPage(desiredPage, (boolean) monospace); // get your formatted page, for console or chat area
 * 
 * see each method javadoc for additional details 
 * 
 * @version 5
 * @author atesin#gmail,com
 * </pre>
 */
public class TabText{
	
	private int chatHeight;
	private int[] tabs;
	private int numPages;
	private String[] lines;
	private Map<Integer, String> charList = new HashMap<Integer, String>(){{
		put(-6, "§");
		put(2, "!.,:;i|");
		put(3, "'`l");
		put(4, " I[]t");
		put(5, "\"()*<>fk{}");
		put(7, "@~");
	}};
	
	// CONSTRUCTOR METHOD
	
	public TabText(String multilineString){
		setText(multilineString);
	}
	
	// SETTER METHODS
	
	/** can reuse the object to save resources by calling this method */
	public void setText(String multilineString){
		lines = multilineString.split("\n", -1);
	}
	
	/**
	 * set page height and get number of pages according to it, for later use with getPage()
	 * @param chatHeight lines you want for each page, no more than 10 recommended
	 * @return number of pages your text will have
	 */
	public int setPageHeight(int chatHeight){
		this.chatHeight = chatHeight;
		numPages = (int) Math.ceil((double)lines.length / (double)chatHeight);
		return numPages;
	}
	
	/**
	 * set horizontal positions of "`" separators, considering 6px chars and 53 chars max 
	 * @param tabs an integer list with desired tab column positions
	 */
	public void setTabs(int... tabs){
		int[] tabs2 = new int[tabs.length + 1];
		tabs2[0] = tabs[0];
		for (int i = 1; i < tabs.length; ++i) tabs2[i] = tabs[i] - tabs[i - 1];
		tabs2[tabs.length] = 53 - tabs[tabs.length - 1];
		this.tabs = tabs2;
	}
	
	// REGULAR METHODS
	
	/**
	 *  append chars with its width to be checked too, default width = 6 so you may only use for width != 6 chars 
	 *  @param chars a string with the chars to be added (careful with unicode or ansi chars, do some tests before)
	 *  @param charsWidth horizontal space in pixels each char occupies
	 */
	public void addChars(String charsList, int charsWidth){
		if (charsWidth == 6) return;
		if (!charList.containsKey(charsWidth)) charList.put(charsWidth, "");
		charList.get(charsWidth).concat(charsList);
	}
	
	/**
	 * get your formatted page, for chat area or console
	 * @param page desired page number (0 = all in one), considering preconfigured page height
	 * @param monospace true if fonts are fixed width (for server console) or false if variable with (for chat area)
	 * @return desired formatted, tabbed page
	 */
	public String getPage(int page, boolean monospace){
		
		// get bounds if user wants pages
		int fromLine = (--page) * chatHeight;
		int toLine = (fromLine + chatHeight > lines.length)? lines.length: fromLine + chatHeight;
		if (page < 0){
			fromLine = 0;
			toLine = lines.length;
		}
		
		// prepare lines iteration
		int tab;
		String line;
		String lines2 = "";
		String fields[];
		Object[] field;
		int lineLen;
		int lineLen2;
		// iterate each line
		for (int linePos = fromLine; linePos < toLine; ++linePos){
			
			fields = lines[linePos].split("`", -1);
			line = "";
			lineLen = 0;
			lineLen2 = 0;
			
			// start iterating each field
			for (int fieldPos = 0; fieldPos < fields.length; ++fieldPos){
				
				// add spaces to adjust horizontal position before if needed
				if (!monospace && lineLen % 4 > 1){
					line += '.';
					lineLen += 2;
				}
				// add spaces to fill width needed width too
				while (lineLen < lineLen2){
					line += ' ';
					lineLen += (monospace)? 1: 4;
				}
				
				// get field and set line properties
				tab = (monospace)? tabs[fieldPos]: tabs[fieldPos] * 6;
				field = pxSubstring(fields[fieldPos], tab, monospace);
				line += (String)field[0];
				lineLen += (Integer)field[1];
				lineLen2 += tab;
			}
			lines2 += (lines2.length() < 1)? line: '\n'+line;
		}
		return lines2;
	}
	
	// PIXEL WIDTH CALCULATION METHODS
	
	/**
	 * returns substring, in chars or pixels, considering format codes
	 * @param str input string
	 * @param len desired string length
	 * @param mono true if lenght will be in chars (for console) or false if will be in pixels (for chat area)
	 * @return object array with stripped string [0] and integer length in pixels or chars depending of mono
	 */
	private Object[] pxSubstring(String str, int len, boolean mono){
		
		int len2 = 0;
		int len3 = 0;
		int len4 = 0;
		
		for (char ch: str.toCharArray()){
			len3 += pxLen(ch, mono);
			if (len3 > len) break;
			++len4;
			len2 = len3;
		}
		return new Object[]{str.substring(0, len4), len2};
	}
	
	/**
	 * returns character total width, considering format codes, internal use
	 * @param ch the character to check
	 * @param mono true for result in chars or false for result in pixels
	 * @return character width depending of "mono"
	 */
	private int pxLen(char ch, boolean mono){
		if (mono) return (ch == '§')? -1: 1;
		// character list iteration, 6 = default
		int l = 6;
		for (int px: charList.keySet()){
			if (charList.get(px).indexOf(ch) >= 0){
				l = px;
				break;
			}
		}
		return l;
	}
	
	/**
	 * sort lines by column values, string or numeric, IF SORT BY DECIMALS IT MUST HAVE SAME DECIMAL POSITIONS
	 * @param keys a list of column numbers criteria where 1 represents first columnn and so, negative numbers means descending order
	 */
	public void sortByFields(int... keys){
		
		// get indexes and sort orders first
		boolean[] desc = new boolean[keys.length];
		
		for (int i = 0; i < keys.length; ++i){
			// get field and direction
			desc[i] = (keys[i] < 0);
			keys[i] = Math.abs(keys[i]) - 1;
		}
		
		// iterate lines
		String[] fields;
		String line;
		String field;
		String field2;
		double num;
		boolean desc2;
		String[] lines2 = new String[lines.length];
		
		for (int i = 0; i < lines.length; ++i){
			
			// iterate fields
			fields = lines[i].replaceAll("§.", "").split("`", -1);
			line = "";
			
			for (int j = 0; j < keys.length; ++j){
				
				// probe if field looks like a number
				field = fields[keys[j]];
				field2 = "~";
				
				try{
					num = Double.parseDouble(field);
					for (int k = field.length(); k < 53; ++k) field2 += " ";
					field2 += field;
					
					// reverse order if negative number
					desc2 = (num < 0)? (desc[j] == false): desc[j];
				}
				catch (NumberFormatException e){
					field2 += field;
					for (int k = field.length(); k < 53; ++k) field2 += " ";
					desc2 = desc[j];
				}
				
				// reverse field char values if reverse order (like in rot13)
				if (desc2){
					field = "";
					for (char c: field2.toCharArray()) field += (char)(158 - c);
					field2 = field;
				}
				
				// add field to line
				line += field2;
			}
			
			// add line to lines
			lines2[i] = line+"`"+i;
		}

		// sort lines
		Arrays.sort(lines2);
		
		// rearrange and set lines
		String[] lines3 = new String[lines.length];
		
		for (int i = 0; i < lines.length; ++i){
			fields = lines2[i].split("`", -1);
			lines3[i] = lines[Integer.parseInt(fields[1])];
		}
		lines = lines3;
	}
}
