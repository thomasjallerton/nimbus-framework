import React from 'react';
import {FileBucketList} from './FileBucketList'

import './FileBucketsPage.css';

function FileBucketsPage({match}) {
  return (
      <div className="content">
        <h2>File Buckets</h2>
        <FileBucketList match={match}/>
      </div>
  );
}

export default FileBucketsPage;
