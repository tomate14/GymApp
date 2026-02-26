import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { SearchableFieldConfig, SearchableSelectValue } from '../../class/searchable-select.model';

type SearchableItem = Record<string, unknown>;

@Component({
  selector: 'app-searchable-select',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './searchable-select.component.html',
  styleUrl: './searchable-select.component.css'
})
export class SearchableSelectComponent {
  private _items: SearchableItem[] = [];

  @Input()
  set items(value: SearchableItem[]) {
    this._items = value ?? [];
    this.applyFilter();
  }
  get items(): SearchableItem[] {
    return this._items;
  }

  @Input() valueField = 'id';
  @Input() labelField = 'name';
  @Input() filterFields: SearchableFieldConfig[] = [];
  @Input() value: SearchableSelectValue = null;
  @Input() searchPlaceholder = 'Buscar...';
  @Input() selectPlaceholder = 'Seleccionar opcion';
  @Output() valueChange = new EventEmitter<SearchableSelectValue>();

  searchTerm = '';
  filteredItems: SearchableItem[] = [];

  onSearchTermChange(): void {
    this.applyFilter();
  }

  onSelectValueChange(rawValue: string): void {
    if (rawValue === '') {
      this.valueChange.emit(null);
      return;
    }

    const firstValue = this.items.length > 0 ? this.items[0][this.valueField] : null;
    if (typeof firstValue === 'number') {
      this.valueChange.emit(Number(rawValue));
      return;
    }

    this.valueChange.emit(rawValue);
  }

  optionValue(item: SearchableItem): string {
    const value = item[this.valueField];
    if (value === null || value === undefined) {
      return '';
    }

    return String(value);
  }

  optionLabel(item: SearchableItem): string {
    const value = item[this.labelField];
    return value === null || value === undefined ? '' : String(value);
  }

  selectedValueAsString(): string {
    if (this.value === null || this.value === undefined) {
      return '';
    }

    return String(this.value);
  }

  readonly trackByValue = (_: number, item: SearchableItem): string => this.optionValue(item);

  private applyFilter(): void {
    const term = this.searchTerm.trim().toLowerCase();
    if (term === '') {
      this.filteredItems = [...this.items];
      return;
    }

    const isNumericTerm = /^\d+$/.test(term);
    const selectedFields = this.getFieldsForTermType(isNumericTerm);
    this.filteredItems = this.items.filter((item) =>
      selectedFields.some((fieldConfig) => {
        const raw = item[fieldConfig.field];
        if (raw === null || raw === undefined) {
          return false;
        }

        return String(raw).toLowerCase().includes(term);
      })
    );
  }

  private getFieldsForTermType(isNumericTerm: boolean): SearchableFieldConfig[] {
    const filtered = this.filterFields.filter((field) => field.type === (isNumericTerm ? 'number' : 'text'));
    if (filtered.length > 0) {
      return filtered;
    }

    if (this.filterFields.length > 0) {
      return this.filterFields;
    }

    return [{ field: this.labelField, type: 'text' }];
  }
}
