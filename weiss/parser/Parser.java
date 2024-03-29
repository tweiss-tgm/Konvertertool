package weiss.parser;

import java.util.ArrayList;

import weiss.csvp.CSVPColumn;
import weiss.csvp.CSVPMeta;
import weiss.csvp.CSVPObject;
import weiss.csvp.CSVPTypeEnum;
import weiss.tokenizer.Token;
import weiss.tokenizer.TokenTypeEnum;

/**
 * Token-Parser, nimmt Tokens und wandelt diese in CSVPMeta,CSVPColumn und CSVPObject - Objekte um
 * @author tweiss
 * @version 2019-06-04
 */
public class Parser {
	private ArrayList<Token> tokens;
	private CSVPMeta meta;
	
	private static int metaend = 0;
	
	public Parser(ArrayList<Token> tokens) {
		this.meta = new CSVPMeta();
		this.tokens = tokens;
	}
	
	public int checkSyntax() {
		
		return -1;
	}
	
	/**
	 * DECL - OPER - DECL - OPER - NEWL
	 * @return
	 */
	public CSVPMeta buildMeta() {
		int newline = 0;
		boolean found = false;
		int columnnumber = 0;
		
		ArrayList<String> columnnames = new ArrayList<>();
		ArrayList<CSVPTypeEnum> columntypes = new ArrayList<>();
		
		for(int i = 0; i < tokens.size(); i++) {
			Token curr = tokens.get(i);
			if(curr.getTokentype() == TokenTypeEnum.NEWLINE) {
				newline = i;
				break;
			}
			if(curr.getTokentype() == TokenTypeEnum.DECLARATION) {
				columnnumber++;
				columnnames.add(curr.getValue());
			}
		}
		
		String max = "";
		String min = "";
		
		for(int i = newline+1; i < (newline*2)+2; i++) {
			Token curr = tokens.get(i);
			if(curr.getTokentype() == TokenTypeEnum.DECLARATION){
				CSVPTypeEnum type = null;
				if(curr.getValue().contains("GANZZAHL") || curr.getValue().contains("TEXT")) {
					if(curr.getValue().contains("GANZZAHL")) {
						type = CSVPTypeEnum.GANZZAHL;
					}else if(curr.getValue().contains("TEXT")) {
						type = CSVPTypeEnum.TEXT;
					}
					
					String value = curr.getValue();
					min = value.substring(value.indexOf('(')+1, value.indexOf(','));
					max = value.substring(value.indexOf(',')+1, value.indexOf(')'));
					
					type.setMin(Integer.parseInt(min));
					type.setMax(Integer.parseInt(max));
				}
				columntypes.add(type);
			}
			if(curr.getTokentype() == TokenTypeEnum.NEWLINE) {
				metaend = i;
				break;
			}
		}
		
		this.meta.setSpaltenanzahl(columnnumber);
		
		for(int i = 0; i < columnnumber; i++) {
			this.meta.addColumn(columnnames.get(i), columntypes.get(i));
		}
		
		return this.meta;
	}
	
	public void buildData() {
		int rownumber = 0;
		int columnpos = 0;
		
		for(int i = metaend+1; i < tokens.size(); i++) {
			Token curr = tokens.get(i);
			CSVPColumn currColumn = this.meta.getSpalten().get(columnpos);
			
			if(curr.getTokentype() != TokenTypeEnum.OPERATOR && curr.getTokentype() != TokenTypeEnum.NEWLINE) {
				CSVPObject data = new CSVPObject(curr.getValue(), rownumber);
				data.setColumn(currColumn);
				currColumn.addData(data);
			}
			
			if(curr.getTokentype() == TokenTypeEnum.OPERATOR) {
				columnpos++;
			}
			
			if(curr.getTokentype() == TokenTypeEnum.NEWLINE) {
				rownumber++;
				columnpos = 0;
			}
		}
		this.meta.setZeilenanzahl(rownumber);
	}
}
