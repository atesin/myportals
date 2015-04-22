package cl.netgamer.tabtext;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class TabText
{
  private int chatHeight;
  private int[] tabs;
  private int numPages;
  private String[] lines;
  private Map<Integer, String> charList = new HashMap() {};
  
  public TabText(String multilineString)
  {
    setText(multilineString);
  }
  
  public void setText(String multilineString)
  {
    this.lines = multilineString.split("\n", -1);
  }
  
  public int setPageHeight(int chatHeight)
  {
    this.chatHeight = chatHeight;
    this.numPages = ((int)Math.ceil(this.lines.length / chatHeight));
    return this.numPages;
  }
  
  public void setTabs(int... tabs)
  {
    int[] tabs2 = new int[tabs.length + 1];
    tabs2[0] = tabs[0];
    for (int i = 1; i < tabs.length; i++) {
      tabs[i] -= tabs[(i - 1)];
    }
    tabs2[tabs.length] = (53 - tabs[(tabs.length - 1)]);
    this.tabs = tabs2;
  }
  
  public void addChars(String charsList, int charsWidth)
  {
    if (charsWidth == 6) {
      return;
    }
    if (!this.charList.containsKey(Integer.valueOf(charsWidth))) {
      this.charList.put(Integer.valueOf(charsWidth), "");
    }
    ((String)this.charList.get(Integer.valueOf(charsWidth))).concat(charsList);
  }
  
  public String getPage(int page, boolean monospace)
  {
    page--;int fromLine = page * this.chatHeight;
    int toLine = fromLine + this.chatHeight > this.lines.length ? this.lines.length : fromLine + this.chatHeight;
    if (page < 0)
    {
      fromLine = 0;
      toLine = this.lines.length;
    }
    String lines2 = "";
    for (int linePos = fromLine; linePos < toLine; linePos++)
    {
      String[] fields = this.lines[linePos].split("`", -1);
      String line = "";
      int lineLen = 0;
      int lineLen2 = 0;
      for (int fieldPos = 0; fieldPos < fields.length; fieldPos++)
      {
        if ((!monospace) && (lineLen % 4 > 1))
        {
          line = line + '.';
          lineLen += 2;
        }
        while (lineLen < lineLen2)
        {
          line = line + ' ';
          lineLen += (monospace ? 1 : 4);
        }
        int tab = monospace ? this.tabs[fieldPos] : this.tabs[fieldPos] * 6;
        Object[] field = pxSubstring(fields[fieldPos], tab, monospace);
        line = line + (String)field[0];
        lineLen += ((Integer)field[1]).intValue();
        lineLen2 += tab;
      }
      lines2 = lines2 + (lines2.length() < 1 ? line : new StringBuilder(String.valueOf('\n')).append(line).toString());
    }
    return lines2;
  }
  
  private Object[] pxSubstring(String str, int len, boolean mono)
  {
    int len2 = 0;
    int len3 = 0;
    int len4 = 0;
    for (char ch : str.toCharArray())
    {
      len3 += pxLen(ch, mono);
      if (len3 > len) {
        break;
      }
      len4++;
      len2 = len3;
    }
    return new Object[] { str.substring(0, len4), Integer.valueOf(len2) };
  }
  
  private int pxLen(char ch, boolean mono)
  {
    if (mono) {
      return ch == 'ง' ? -1 : 1;
    }
    int l = 6;
    for (Iterator localIterator = this.charList.keySet().iterator(); localIterator.hasNext();)
    {
      int px = ((Integer)localIterator.next()).intValue();
      if (((String)this.charList.get(Integer.valueOf(px))).indexOf(ch) >= 0)
      {
        l = px;
        break;
      }
    }
    return l;
  }
  
  public void sortByFields(int... keys)
  {
    boolean[] desc = new boolean[keys.length];
    for (int i = 0; i < keys.length; i++)
    {
      //desc[i] = (keys[i] < 0 ? 1 : false);
      desc[i] = (keys[i] < 0);
      keys[i] = (Math.abs(keys[i]) - 1);
    }
    String[] lines2 = new String[this.lines.length];
    for (int i = 0; i < this.lines.length; i++)
    {
      String[] fields = this.lines[i].replaceAll("ยง.", "").split("`", -1);
      String line = "";
      for (int j = 0; j < keys.length; j++)
      {
        String field = fields[keys[j]];
        String field2 = "~";
        int k;
        boolean desc2;
        try
        {
          double num = Double.parseDouble(field);
          for (k = field.length(); k < 53; k++) {
            field2 = field2 + " ";
          }
          field2 = field2 + field;
          

          desc2 = num < 0.0D ? (desc[j] == false) : desc[j];
        }
        catch (NumberFormatException e)
        {
          field2 = field2 + field;
          for (k = field.length(); k < 53; k++) {
            field2 = field2 + " ";
          }
          desc2 = desc[j];
        }
        if (desc2)
        {
          field = "";
          for (char c : field2.toCharArray()) {
            field = field + (char)(158 - c);
          }
          field2 = field;
        }
        line = line + field2;
      }
      lines2[i] = (line + "`" + i);
    }
    Arrays.sort(lines2);
    

    String[] lines3 = new String[this.lines.length];
    for (int i = 0; i < this.lines.length; i++)
    {
      String[] fields = lines2[i].split("`", -1);
      lines3[i] = this.lines[Integer.parseInt(fields[1])];
    }
    this.lines = lines3;
  }
}


/* Location:           C:\Users\AT-HE\Desktop\games-setup\minecraft\bukkit\1.8.3\MyPortals.jar
 * Qualified Name:     cl.netgamer.tabtext.TabText
 * JD-Core Version:    0.7.0.1
 */