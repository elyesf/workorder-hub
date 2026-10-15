import { StatusLabelPipe } from './status-label.pipe';

describe('StatusLabelPipe', () => {
  const pipe = new StatusLabelPipe();

  it('turns enum values into readable labels', () => {
    expect(pipe.transform('IN_PROGRESS')).toBe('In progress');
    expect(pipe.transform('DONE')).toBe('Done');
  });

  it('returns an empty string for missing values', () => {
    expect(pipe.transform(null)).toBe('');
  });
});
