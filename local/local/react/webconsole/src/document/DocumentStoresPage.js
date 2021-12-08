import React from 'react';
import {DocumentStoresList} from './DocumentStoresList'
import './DocumentStoresPage.css';

function DocumentStoresPage({match}) {
    return (
        <div className="content">
            <h2>Document Stores</h2>
            <DocumentStoresList match={match}/>
        </div>
);
}


export default DocumentStoresPage;
