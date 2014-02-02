package sagex.phoenix.lucene;

import org.apache.lucene.document.Document;

/**
 * provides a client/server service for lucene indexing and searching
 * 
 * 
 * 
 * NOTE: maybe have a distributed database... ie server will index a file, then
 * broadcase an index command to listeners... clients would have their own
 * database but would listen for events from the server
 * 
 * 
 * 
 * 
 * @author seans
 * 
 */
public class LuceneService {
	public LuceneService() {
	}

	public Document getDocument(String indexName, String primaryKeyName, String primaryKeyValue) {
		return null;
	}

	public Document updateDocument(String indexName, String primaryKeyName, Document doc) {
		return null;
	}

	/**
	 * need to create an rmi service for adding documents to the index
	 * 
	 * this is needed so that clients can get document values and later update
	 * the values for things like setwatched, iswatched, etc.
	 * 
	 * need to create a generic properties like service, where you can can
	 * set/get properties fields will be name, value. name will be stored
	 * indexed, and value will be stored not indexed.
	 */
}
