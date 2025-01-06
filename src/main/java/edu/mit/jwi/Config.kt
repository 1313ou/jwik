package edu.mit.jwi

import edu.mit.jwi.data.compare.ILineComparator
import java.nio.charset.Charset

class Config {

    var checkLexicalId: Boolean? = null
    var indexSensePattern: String? = null
    var indexNounComparator: ILineComparator? = null
    var indexVerbComparator: ILineComparator? = null
    var indexAdjectiveComparator: ILineComparator? = null
    var indexAdverbComparator: ILineComparator? = null
    var indexSenseKeyComparator: ILineComparator? = null
    var charSet: Charset? = null
}
