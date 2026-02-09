
import { getAllDoclets } from './src/jsdoc-runner.js';

async function run() {
  const doclets = await getAllDoclets('./data', 32, './node_modules/.bin/jsdoc.cmd', '8.14.1');
  const d = doclets.find(d => d.longname === 'anychart.charts.Sunburst#animation');
  console.log('Sunburst#animation found:', !!d);
  if (d) {
      console.log('Kind:', d.kind);
      console.log('InheritDoc:', d.inheritdoc);
      console.log('Params:', d.params);
      console.log('Returns:', d.returns);
  }
}
run();
