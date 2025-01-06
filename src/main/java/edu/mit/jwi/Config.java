package edu.mit.jwi;

import edu.mit.jwi.data.compare.ILineComparator;

import java.nio.charset.Charset;

public class Config
{
    public Boolean checkLexicalId;

    public String indexSensePattern;
    public ILineComparator indexNounComparator;
    public ILineComparator indexVerbComparator;
    public ILineComparator indexAdjectiveComparator;
    public ILineComparator indexAdverbComparator;
    public ILineComparator indexSenseKeyComparator;
    public Charset charSet;
}
