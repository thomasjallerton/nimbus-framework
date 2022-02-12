import React from 'react';
import ReactDOM from 'react-dom';
import FileBucketsPage from './FileBucketsPage';

it('renders without crashing', () => {
  const div = document.createElement('div');
  ReactDOM.render(<FileBucketsPage />, div);
  ReactDOM.unmountComponentAtNode(div);
});
