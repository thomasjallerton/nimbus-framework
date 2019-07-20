import React from 'react';
import {KeyValueStoresList} from './KeyValueStoresList'

import './KeyValueStoresPage.css';

function KeyValueStoresPage({match}) {
    return (
        <div className="content">
            <h2>Key-Value Stores</h2>
            <KeyValueStoresList match={match}/>
        </div>
    );
}

export default KeyValueStoresPage;
