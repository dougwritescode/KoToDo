package com.matrix.kotodo.documents

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import com.matrix.kotodo.R
import com.matrix.kotodo.documents.ProviderConfig.DEFAULT_DOCUMENT_PROJECTION
import com.matrix.kotodo.documents.ProviderConfig.DEFAULT_ROOT_PROJECTION
import com.matrix.kotodo.documents.ProviderConfig.ROOT_FOLDER_ID
import com.matrix.kotodo.documents.ProviderConfig.ROOT_ID
import java.io.File
import java.io.FileOutputStream

class LocalDocumentsProvider: DocumentsProvider() {

    override fun onCreate(): Boolean {
        return false
    }

    override fun queryRoots(projection: Array<out String>?): Cursor {
        return MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION)
            .apply {
                val row = newRow()
                with(row) {
                    add(DocumentsContract.Root.COLUMN_ROOT_ID, ROOT_ID)
                    add(DocumentsContract.Root.COLUMN_ICON, R.drawable.ic_launcher_foreground)
                    add(DocumentsContract.Root.COLUMN_TITLE, context?.getString(R.string.app_name) ?: "NoAppName")
                    add(DocumentsContract.Root.COLUMN_FLAGS, 0)
                    add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, ROOT_FOLDER_ID)
                }
            }
    }

    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
        if (documentId == ROOT_FOLDER_ID) {
            val row = cursor.newRow()
            with(row) {
                add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, ROOT_FOLDER_ID)
                add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR)
                add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, context?.getString(R.string.app_name) ?: "NoAppName")
                add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, null)
                add(DocumentsContract.Document.COLUMN_FLAGS, 0)
                add(DocumentsContract.Document.COLUMN_SIZE, null)
            }
        }
        return cursor
    }

    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?,
    ): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)

        if (parentDocumentId == ROOT_FOLDER_ID) {
            for (i in 0..10) {
                val row = cursor.newRow()
                with(row) {
                    add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, i.toShort())
                    add(DocumentsContract.Document.COLUMN_MIME_TYPE, if (i == 0) DocumentsContract.Document.MIME_TYPE_DIR else "text/plain")
                    add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, "$i")
                    add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, 0)
                    add(DocumentsContract.Document.COLUMN_FLAGS, 0)
                    add(DocumentsContract.Document.COLUMN_SIZE, 10)
                }

            }
        } else {
            for (i in 0..2) {
                val row = cursor.newRow()
                with(row) {
                    add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, i.toShort())
                    add(DocumentsContract.Document.COLUMN_MIME_TYPE, "text/plain")
                    add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, "$i")
                    add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, 0)
                    add(DocumentsContract.Document.COLUMN_FLAGS, 0)
                    add(DocumentsContract.Document.COLUMN_SIZE, 10)
                }

            }
        }

        return cursor
    }

    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?,
    ): ParcelFileDescriptor {
        val file = File(context?.filesDir, "todo.txt")
        FileOutputStream(file).use { output ->
            val buffer = documentId?.toByteArray()
            buffer?.size?.let { output.write(buffer, 0, it) }
            output.flush()
        }
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.parseMode(mode))
    }
}