package com.springsource.html5expense.services.utilities;

import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;
import com.mongodb.gridfs.GridFSInputFile;
import com.sun.istack.internal.Nullable;
import org.apache.commons.io.IOUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.DbCallback;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.util.Assert;

import java.io.InputStream;

/**
 * Simple utility class to interface with MongoDB's GridFS
 * abstraction, which can be quite handy for storing files.
 *
 * @author Josh Long
 */
abstract public class MongoDbGridFsUtils {

    /**
     * A simple utility to write objects to the database
     *
     * @param mongoTemplate the MongoTemplate instance
     * @param bucket        the name of the collection to store it into
     * @param content       the content to write
     * @param filename      the file name to use for the file (in practice, this could be any String)
     * @param metadata      the metadata to associate with this information (it's optional)
     * @return a {@link GridFSFile} that represents the written data.
     */
    public static GridFSFile write(final MongoTemplate mongoTemplate,
                                   final String bucket,
                                   final InputStream content,
                                   final String filename,
                                   @Nullable final DBObject metadata) {
        Assert.notNull(content);
        Assert.hasText(filename);
        return mongoTemplate.execute(new DbCallback<GridFSInputFile>() {
            @Override
            public GridFSInputFile doInDB(DB db) throws MongoException, DataAccessException {
                GridFSInputFile file = gridFs(db, bucket).createFile(content, filename, true);
                file.setFilename(filename);
                if (null != metadata)
                    file.setMetaData(metadata);
                file.save();
                IOUtils.closeQuietly(content);
                return file;
            }
        });
    }

    /**
     * Reads file data from MongoDB
     *
     * @param mongoTemplate the MongoTemplate is required because, with it, we can access a {@link DB DB} object
     * @param bucket        the name of the collection to put the file
     * @param fileName      the name of the file
     * @return an InputStream that the application can read from.
     */
    public static InputStream read(MongoTemplate mongoTemplate, final String bucket, final String fileName) {
        return mongoTemplate.executeInSession(new DbCallback<InputStream>() {
            @Override
            public InputStream doInDB(DB db) throws MongoException, DataAccessException {
                GridFS gridFS = gridFs(db, bucket);
                GridFSDBFile file = gridFS.findOne(fileName);
                return file.getInputStream();
            }
        });
    }

    private static GridFS gridFs(DB db, String bucket) {
        return new GridFS(db, bucket);
    }
}
